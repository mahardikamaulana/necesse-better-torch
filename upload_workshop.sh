#!/bin/bash
set -e

# ==============================================================================
# Steam Workshop Upload Script for Necesse Mod: "Let there be Light!"
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

APP_ID="1169040"
VDF_FILE="$SCRIPT_DIR/workshop_item.vdf"
PREVIEW_FILE="$SCRIPT_DIR/src/main/resources/preview.png"
WORKSHOP_DIR="$SCRIPT_DIR/build/workshop"
JAR_DIR="$SCRIPT_DIR/build/jar"

echo "========================================================"
echo "   Necesse Mod Steam Workshop Uploader: Let there be Light!"
echo "========================================================"

# 1. Check for SteamCMD installation
STEAMCMD=""
if command -v steamcmd &> /dev/null; then
    STEAMCMD="steamcmd"
elif [ -f "$HOME/Library/Application Support/Steam/steamcmd/steamcmd.sh" ]; then
    STEAMCMD="$HOME/Library/Application Support/Steam/steamcmd/steamcmd.sh"
elif [ -f "$HOME/steamcmd/steamcmd.sh" ]; then
    STEAMCMD="$HOME/steamcmd/steamcmd.sh"
elif [ -f "/usr/local/bin/steamcmd" ]; then
    STEAMCMD="/usr/local/bin/steamcmd"
fi

if [ -z "$STEAMCMD" ]; then
    echo "[!] SteamCMD was not found in PATH or standard locations."
    echo "    Please install steamcmd or provide the full path to steamcmd.sh."
    read -rp "Enter path to steamcmd (or steamcmd.sh): " STEAMCMD
    if [ ! -f "$STEAMCMD" ] && ! command -v "$STEAMCMD" &> /dev/null; then
        echo "[ERROR] Invalid SteamCMD path: $STEAMCMD"
        exit 1
    fi
fi

echo "[✓] SteamCMD found: $STEAMCMD"

# 2. Build fresh mod JAR
echo ""
echo "[*] Building fresh mod JAR..."
./gradlew clean build

# Find compiled JAR
JAR_FILE=$(find "$JAR_DIR" -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    echo "[ERROR] Compiled JAR not found in $JAR_DIR"
    exit 1
fi
echo "[✓] Found compiled JAR: $(basename "$JAR_FILE")"

# 3. Prepare workshop directory
echo ""
echo "[*] Staging workshop directory at $WORKSHOP_DIR..."
rm -rf "$WORKSHOP_DIR"
mkdir -p "$WORKSHOP_DIR"
cp "$JAR_FILE" "$WORKSHOP_DIR/"

if [ ! -f "$PREVIEW_FILE" ]; then
    echo "[ERROR] Preview image not found at $PREVIEW_FILE"
    exit 1
fi
echo "[✓] Workshop folder staged with $(basename "$JAR_FILE") and preview image verified."

# 4. Published File ID prompt
CURRENT_FILE_ID="0"
if [ -f "$VDF_FILE" ]; then
    EXTRACTED_ID=$(grep -o '"publishedfileid"[[:space:]]*"[^"]*"' "$VDF_FILE" | awk -F'"' '{print $4}')
    if [ -n "$EXTRACTED_ID" ] && [ "$EXTRACTED_ID" != "0" ]; then
        CURRENT_FILE_ID="$EXTRACTED_ID"
    fi
fi

echo ""
echo "--------------------------------------------------------"
if [ "$CURRENT_FILE_ID" = "0" ]; then
    echo "Current Target: NEW Workshop Item (publishedfileid: 0)"
else
    echo "Current Target: UPDATE existing item (publishedfileid: $CURRENT_FILE_ID)"
fi
echo "--------------------------------------------------------"

read -rp "Enter Workshop PublishedFileID [press Enter for $CURRENT_FILE_ID]: " USER_FILE_ID
PUBLISHED_ID="${USER_FILE_ID:-$CURRENT_FILE_ID}"

read -rp "Enter Change Note (optional, press Enter to use default): " USER_CHANGE_NOTE
CHANGE_NOTE="${USER_CHANGE_NOTE:-Release v1.0.0 for Necesse 1.3.3}"

# 5. Update workshop_item.vdf
sed -i '' -e "s|\"publishedfileid\"[[:space:]]*\"[^\"]*\"|\"publishedfileid\"   \"$PUBLISHED_ID\"|g" "$VDF_FILE" 2>/dev/null || \
sed -i -e "s|\"publishedfileid\"[[:space:]]*\"[^\"]*\"|\"publishedfileid\"   \"$PUBLISHED_ID\"|g" "$VDF_FILE"

sed -i '' -e "s|\"contentfolder\"[[:space:]]*\"[^\"]*\"|\"contentfolder\"     \"$WORKSHOP_DIR\"|g" "$VDF_FILE" 2>/dev/null || \
sed -i -e "s|\"contentfolder\"[[:space:]]*\"[^\"]*\"|\"contentfolder\"     \"$WORKSHOP_DIR\"|g" "$VDF_FILE"

sed -i '' -e "s|\"previewfile\"[[:space:]]*\"[^\"]*\"|\"previewfile\"       \"$PREVIEW_FILE\"|g" "$VDF_FILE" 2>/dev/null || \
sed -i -e "s|\"previewfile\"[[:space:]]*\"[^\"]*\"|\"previewfile\"       \"$PREVIEW_FILE\"|g" "$VDF_FILE"

sed -i '' -e "s|\"changenote\"[[:space:]]*\"[^\"]*\"|\"changenote\"        \"$CHANGE_NOTE\"|g" "$VDF_FILE" 2>/dev/null || \
sed -i -e "s|\"changenote\"[[:space:]]*\"[^\"]*\"|\"changenote\"        \"$CHANGE_NOTE\"|g" "$VDF_FILE"

echo "[✓] Updated $VDF_FILE with publishedfileid: $PUBLISHED_ID"

# 6. Steam login & upload execution
echo ""
STEAM_USERNAME="${STEAM_USER:-}"
if [ -z "$STEAM_USERNAME" ]; then
    read -rp "Enter your Steam Username: " STEAM_USERNAME
fi

if [ -z "$STEAM_USERNAME" ]; then
    echo "[ERROR] Steam username cannot be empty."
    exit 1
fi

echo ""
echo "[*] Launching SteamCMD to build and upload workshop item..."
echo "    Command: $STEAMCMD +login $STEAM_USERNAME +workshop_build_item \"$VDF_FILE\" +quit"
echo ""

"$STEAMCMD" +login "$STEAM_USERNAME" +workshop_build_item "$VDF_FILE" +quit

echo ""
echo "========================================================"
echo "[✓] Workshop upload process finished!"
echo "========================================================"
