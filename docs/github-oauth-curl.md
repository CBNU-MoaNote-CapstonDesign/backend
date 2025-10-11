# GitHub OAuth 및 연동 API 테스트 cURL 스크립트

> `{{BASE_URL}}` 는 백엔드 서버 주소(예: `http://localhost:8080`)로 치환하세요.

## 1. Authorization URL 요청

```bash
curl -X POST "{{BASE_URL}}/api/github/oauth/authorize" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<USER_UUID>"
  }'
```

성공 시 다음과 같은 응답을 받습니다.

```json
{
  "authorizationUrl": "https://github.com/login/oauth/authorize?...",
  "state": "<STATE_FROM_SERVER>"
}
```

프론트엔드는 `authorizationUrl` 로 리다이렉트하고, GitHub 콜백에 포함된 `code` 와 `state` 를 백엔드에 전달합니다.

## 2. Authorization Code 교환

```bash
curl -X POST "{{BASE_URL}}/api/github/oauth/callback" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<USER_UUID>",
    "code": "<CODE_FROM_GITHUB>",
    "state": "<STATE_FROM_SERVER>"
  }'
```

응답이 `204 No Content` 이면 토큰이 저장되었습니다.

## 3. 저장소 불러오기 (저장된 토큰 사용)

```bash
curl -X POST "{{BASE_URL}}/api/github/import" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<USER_UUID>",
    "repositoryUrl": "https://github.com/<owner>/<repo>.git"
  }'
```

## 4. 브랜치 생성 및 커밋 푸시

```bash
curl -X POST "{{BASE_URL}}/api/github/branch" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<USER_UUID>",
    "repositoryUrl": "https://github.com/<owner>/<repo>.git",
    "baseBranch": "main",
    "branchName": "feature/test",
    "commitMessage": "Add sample",
    "files": {
      "<repo-name>/README.md": "Updated from API"
    }
  }'
```

## 5. 원격 변경 사항 가져오기

```bash
curl -X POST "{{BASE_URL}}/api/github/fetch" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<USER_UUID>",
    "repositoryUrl": "https://github.com/<owner>/<repo>.git",
    "branchName": "main"
  }'
```

토큰이 저장되지 않은 경우 400 응답을 받을 수 있으므로, 먼저 OAuth 플로우를 완료해야 합니다.
