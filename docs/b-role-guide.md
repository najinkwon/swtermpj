# B 담당 작업 범위

## B 담당
- Room DB
- Repository
- UseCase
- ViewModel
- 머신러닝 재료 카테고리 분류
- 레시피 추천 로직
- 쇼핑 API 연결
- 부족 재료 알림
- OCR 파싱 구조

## A 담당과 연결해야 하는 ViewModel
- BHomeViewModel
- BIngredientViewModel
- BRecipeViewModel
- BShoppingViewModel
- BReceiptViewModel
- BNotificationViewModel

## A 담당자가 주로 호출할 함수

### 재료 추가
BIngredientViewModel.addIngredient(
    name,
    initialAmount,
    currentAmount,
    unit,
    expiryDate,
    storageType
)

### 재료 목록
BIngredientViewModel.ingredients

### 레시피 추천
BRecipeViewModel.recommend("한식")
BRecipeViewModel.recipes

### 쇼핑 검색
BShoppingViewModel.search("간장")
BShoppingViewModel.items

### 영수증 OCR 결과 테스트
BReceiptViewModel.extractFromRawText(rawText)
BReceiptViewModel.candidates

## 주의
build.gradle.kts, AndroidManifest.xml, MainActivity.kt는 공통 파일이므로 수정 전에 팀원과 상의한다.
