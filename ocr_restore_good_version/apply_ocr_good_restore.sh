set -e

ROOT="$(pwd)"
PKG_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKUP="/tmp/naengteol-ocr-good-restore-$(date +%Y%m%d-%H%M%S)"

mkdir -p "$BACKUP"

test -d "$ROOT/app/src/main/java/com/example/swtermproject" || {
  echo "ERROR: Android project root에서 실행해야 합니다."
  exit 1
}

mkdir -p "$BACKUP/app/src/main/java/com/example/swtermproject/ocr"
mkdir -p "$BACKUP/app/src/main/java/com/example/swtermproject/ui/ingredient"

for f in \
app/src/main/java/com/example/swtermproject/ocr/BReceiptOcrManager.kt \
app/src/main/java/com/example/swtermproject/ocr/BReceiptItemExtractor.kt \
app/src/main/java/com/example/swtermproject/ocr/BReceiptItemCandidate.kt \
app/src/main/java/com/example/swtermproject/ocr/BReceiptParser.kt \
app/src/main/java/com/example/swtermproject/ocr/BAmountNormalizer.kt \
app/src/main/java/com/example/swtermproject/ocr/BIngredientDictionary.kt \
app/src/main/java/com/example/swtermproject/ui/ingredient/AReceiptScanFragment.kt
do
  if [ -f "$ROOT/$f" ]; then
    mkdir -p "$BACKUP/$(dirname "$f")"
    cp "$ROOT/$f" "$BACKUP/$f"
  fi
done

cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BReceiptOcrManager.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BReceiptOcrManager.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BReceiptItemExtractor.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BReceiptItemExtractor.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BReceiptItemCandidate.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BReceiptItemCandidate.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BReceiptParser.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BReceiptParser.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BAmountNormalizer.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BAmountNormalizer.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ocr/BIngredientDictionary.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ocr/BIngredientDictionary.kt"
cp "$PKG_DIR/files/app/src/main/java/com/example/swtermproject/ui/ingredient/AReceiptScanFragment.kt" "$ROOT/app/src/main/java/com/example/swtermproject/ui/ingredient/AReceiptScanFragment.kt"

grep -R "extractSafeReceiptCandidates\|safeReceiptRules\|ocr-safe" -n "$ROOT/app/src/main/java/com/example/swtermproject" && {
  echo "ERROR: safe OCR 임시 코드가 아직 남아 있습니다."
  echo "위 grep 결과를 보내주세요."
  exit 1
} || true

echo "OCR restored from good source."
echo "Backup: $BACKUP"

./gradlew clean assembleDebug
