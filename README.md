## CK Remote - 코어키퍼 리모트 플레이어 클라이언트

Compose Multiplatform 으로 작성된, 코어키퍼 리모트 플레이어 클라이언트입니다.  
리모트 서버로부터 미디어를 받아 플레이하고, UI 를 통해 들어오는 가상 키보드 입력을 리모트로 보내는 역할을 합니다.


### TL;DR

**아직은 사용할 수 없습니다.** 리모트 서버와 관련한 저장소 및 설치파일을 _아직_ 공개하지 않고 있고, 별도 TURN 서버의 구성 또한 쉽지 않기 때문입니다.  

이후에 리모트 서버 관련 저장소와 설치파일을 추가하면 그 때 조금 더 상세한 내용을 기술할 예정입니다.


### 코드에 대해

하나의 `composeApp` 모듈에 `commonMain` 및 `androidMain`/`iosMain` 소스셋으로 분리되며, 각각 공통로직, android 의존 로직, iOS 의존 로직이 포함됩니다.

시그널링 로직은 Ktor를 사용하여 구현되었고, Ktor 는 [Common 에서 지원](https://ktor.io/docs/client-supported-platforms.html)하기 때문에 commonMain 에 있습니다.  
WebRTC 연결 로직 및 스트림 표현 로직은 Android 에서 libwebrtc.jar 를, iOS 에서 GoogleWebRTC cocoapod 모듈을 사용하여 작성되었고, 때문에 공통로직으로 분리할 수 없어 각 의존 소스셋에 있습니다.

이 둘을 잇는 클래스는 `Communicator` 이며, `commonMain` 을 포함한 각 소스셋에 `Communicator[.platform].kt` 파일로부터 확인할 수 있습니다.

앱 바로가기 핸들링과 관련해서 Android 의 `MainActivity` 와 iOS 의 `AppDelegate`(+`SceneDelegate`) 를 일부 연결하는 코드 또한 각 플랫폼 의존 소스셋에 있습니다.  
앱 바로가기 자체에 대한 핸들링은 `core` 패키지의 `AppShortcutHandling`에, 그것을 눌렀을 때 `MainActivity` 및 `AppDelegate`(+`SceneDelegate`) 가 전달받는 값들에 대한 핸들링은 `<root>` 패키지의 `LaunchArguments`에 있습니다.

UI 로직은 위에서 언급한 스트림 표현 UI 및 피드에서 표현되는 WebView 를 제외하면 모두 commonMain 에 있습니다.

인게임 컨트롤러와 관련된 것들은 `PlayerView.kt` 와 `ui.controller` 패키지에, 메인 화면과 관련된 것들은 `MainOverlay.kt` 와 `ui.overlay` 패키지에 있습니다.

메인 화면의 피드와 관련된 것들은 `core.feeds` 패키지에 있으며, 대부분 Ktor 를 사용한 Api Fetching 과 그들이 응답한 게시글의 내용을 파싱하는 로직입니다.


### 빌드에 대해

우선 사용하려면 리모트 서버가 필요하나 관련 저장소 및 설치파일을 공개하지 않은 바, 당장은 빌드 결과물을 통해 실제적인 사용은 어렵습니다.

다만 iOS 는 몇 가지 빌드에 필요한 값들을 설정해주어야 하는데, 아래와 같습니다:

- iosApp/Configuration/Config.xcconfig
  TEAM_ID 에 자신의 TeamID 를 작성해야합니다.  
  자신의 팀 아이디가 뭔지 모른다면 `kdoctor --team-ids` 명령으로 찾을 수 있습니다.  
  만약 이 명령이 아무것도 출력하지 않는다면, 아직 애플 개발 환경이 제대로 설정되지 않은 것이므로 별도 Kotlin Multiplatform 개발 문서가 기술하는 사항을 참고해주세요.

Android 는 별다른 추가 설정 없이도 빌드가 되어야 합니다.


### TURN 서버와 관련하여

추후에 UI 를 통해 turn 서버를 지정할 수 있는 기능을 추가할 예정이나, 아직은 구현되어있지 않습니다.  
이 값들은 `commonMain` 소스셋의 `Constants` 오브젝트에 직접 하드코딩할 수 있으며, 위 기능이 구현되기 전까지 앱을 사용하려면 반드시 이 값을 추가해야합니다.


### 기타

처음으로 어느정도 완성한 Compose Multiplatform 프로젝트입니다.  
다만, 아래와 같은 TODO 가 남아있습니다:

- TURN 서버 하드코딩 없이 사용자로부터 입력받아 적용하기
- 각종 에러 핸들링: 소켓 연결 실패, 네트워크 소실 등의 상황에 대한 대처
