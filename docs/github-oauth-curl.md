# GitHub OAuth 및 연동 API 테스트 cURL 스크립트

## GitHub OAuth 설정 값 안내

| 속성 | 입력해야 하는 값 | 비고 |
| --- | --- | --- |
| `github.oauth.client-id` | GitHub OAuth App 등록 페이지에서 확인할 수 있는 **Client ID** | [GitHub OAuth App](https://github.com/settings/developers) 생성 후 발급됩니다. |
| `github.oauth.client-secret` | GitHub OAuth App 생성 시 발급된 **Client Secret** | 백엔드 서버만 알고 있도록 안전하게 보관하세요. |
| `github.oauth.redirect-uri` | GitHub OAuth App 에 등록한 **Authorization callback URL** | GitHub 개발자 설정의 Callback URL 과 동일해야 합니다. 예시: `http://localhost:8080/oauth/callback` |
| `github.oauth.scope` | 액세스 토큰이 접근할 **권한 범위(scope)** | 저장소 조작이 필요하면 `repo` 를 사용합니다. 필요한 권한에 따라 조정 가능합니다. |
| `github.oauth.state-ttl` | Authorization state 를 메모리에 유지할 시간(예: `10m`) | 기본값은 10분이며, 시간이 지나면 state 는 자동으로 폐기됩니다. |
| `github.oauth.token-ttl` | GitHub 액세스 토큰을 메모리에 유지할 시간(예: `1h`) | 기본값은 1시간이며, 만료되면 다시 OAuth 플로우를 진행해야 합니다. |

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

응답이 `204 No Content` 이면 토큰이 저장되었습니다. 토큰은 백엔드 메모리에만 보관되며 `github.oauth.token-ttl` 설정값이 지나면 만료됩니다.

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
