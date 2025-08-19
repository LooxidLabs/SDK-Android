#!/usr/bin/env bash
set -euo pipefail

# 단일 스크립트: latest-version + gradle.properties 생성 출력
# 기본값: io.github.looxidlabs / SDK-Android
# 옵션:
#   -g <groupId>      (기본: io.github.looxidlabs)
#   -a <artifactId>   (기본: SDK-Android)
#   -d                (디버그 출력)
# 사용:
#   ./gen-gradle-props.sh
#   ./gen-gradle-props.sh -g io.github.looxidlabs -a SDK-Android > gradle.properties

GROUP="io.github.looxidlabs"
ARTIFACT="SDK-Android"
DEBUG=false

usage() {
  cat <<'EOF'
Usage:
  gen-gradle-props.sh [-g <groupId>] [-a <artifactId>] [-d]

Defaults:
  groupId    = io.github.looxidlabs
  artifactId = SDK-Android

Examples:
  ./gen-gradle-props.sh
  ./gen-gradle-props.sh -g io.github.looxidlabs -a SDK-Android > gradle.properties
EOF
}

while getopts "g:a:dh" opt; do
  case "$opt" in
    g) GROUP="$OPTARG" ;;
    a) ARTIFACT="$OPTARG" ;;
    d) DEBUG=true ;;
    h|\?) usage; exit 0 ;;
  esac
done

log() { $DEBUG && echo "[DEBUG] $*" >&2 || true; }

# --- JDK 17 JAVA_HOME 가져오기 ---
JAVA_HOME_PATH="$(
  /usr/libexec/java_home -v 17 2>/dev/null || true
)"
if [[ -z "$JAVA_HOME_PATH" ]]; then
  echo "JDK 17 JAVA_HOME을 찾지 못했습니다. (macOS에 JDK 17 설치/링크 확인)" >&2
  exit 1
fi
log "JAVA_HOME=$JAVA_HOME_PATH"

# --- latest-version 내장 구현 ---
latest_version() {
  local group="$1" artifact="$2"
  local group_path
  group_path="$(printf '%s' "$group" | tr '.' '/')"

  local meta_url="https://repo1.maven.org/maven2/${group_path}/${artifact}/maven-metadata.xml"
  log "META_URL=$meta_url"

  # 1) maven-metadata.xml 시도
  set +e
  local xml
  xml="$(curl -fsS "$meta_url")"
  local code=$?
  set -e
  if [[ $code -eq 0 && -n "$xml" ]]; then
    # 우선 <latest> 시도
    local ver=""
    if command -v xmllint >/dev/null 2>&1; then
      ver="$(printf '%s' "$xml" | xmllint --xpath 'string(//metadata/versioning/latest)' - 2>/dev/null || true)"
    else
      ver="$(printf '%s' "$xml" | grep -oE '<latest>[^<]+' | sed 's/<latest>//' || true)"
    fi
    if [[ -n "$ver" ]]; then
      printf '%s\n' "$ver"
      return 0
    fi
    log "<latest> 비어있음. versions 목록에서 최대값 선택"

    # <latest>가 없으면 versions 중 최댓값(semver-ish) 선택
    local maxv
    maxv="$(printf '%s' "$xml" | grep -oE '<version>[^<]+' | cut -d '>' -f2 | sort -V | tail -n1 || true)"
    if [[ -n "$maxv" ]]; then
      printf '%s\n' "$maxv"
      return 0
    fi
    log "메타데이터 파싱 실패. HTML 폴백 시도"
  else
    log "메타데이터 curl 실패 (code=$code). HTML 폴백 시도"
  fi

  # 2) central.sonatype.com HTML 폴백 (구조 변경에 취약)
  local html_url="https://central.sonatype.com/artifact/${group}/${artifact}"
  log "HTML_URL=$html_url"

  set +e
  local html
  html="$(curl -fsS "$html_url")"
  code=$?
  set -e
  if [[ $code -eq 0 && -n "$html" ]]; then
    # 페이지 헤더의 pkg:maven/group/artifact@<version> 패턴
    local hv
    hv="$(printf '%s' "$html" | grep -oE "pkg:maven/${group}/${artifact}@[0-9A-Za-z._-]+" | head -n1 | cut -d@ -f2 || true)"
    if [[ -n "$hv" ]]; then
      printf '%s\n' "$hv"
      return 0
    fi
  fi

  return 1
}

SDK_VER="$(latest_version "$GROUP" "$ARTIFACT" || true)"
if [[ -z "$SDK_VER" ]]; then
  echo "최신 버전을 찾지 못했습니다. (groupId='${GROUP}', artifactId='${ARTIFACT}')" >&2
  exit 2
fi
log "LATEST_VERSION=$SDK_VER"

# --- properties 형식으로 출력 ---
cat <<EOF
org.gradle.java.home=${JAVA_HOME_PATH}
sdkGroupId=${GROUP}
sdkArtifactId=${ARTIFACT}
sdkVersion=${SDK_VER}
EOF
