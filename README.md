### Api-Tester

#### 実装機能と制約事項

1. Swaggerの配置先を`config.properties`の`oasFilesDir`から読み込む
1. Swaggerの定義に従いAPIリクエストに必要なリクエストパラメータの入力欄を表示し、送信ボタンを押すとAPIをリクエストする
    * Produceは`application/json`または`application/xml`にのみ対応
    * Consumeは`application/x-www-form-urlencoded`または`application/json`にのみ対応
      * jsonの場合は1階層のみ、配列は1要素のみ対応
      * fileタイプ(`multipart/form-data`)には未対応
1. レスポンスの内容を保存し次のリクエストに使用する
    * レスポンスがjsonかつ1階層のみである場合に限る
1. チェックボックスをつけることによりリクエストパラメータを送信しないようにできる
1. Enumの値がある場合は、選択肢を表示する。ただし、テキストボックスに入力した文字列の長さが1以上の場合は、テキストボックスの入力を優先する

#### 内部構造
本プログラムはSpring-Boot + Thymeleafで作成している

ルートパッケージ：`com.github.km91jp.tester.api`

`conf`パッケージ
 * `AppConfig` ... JavaConfig

`ctrl`パッケージ
 * `ApiTester` ... コントローラクラス
    * `http://localhost:8080/`でトップページを表示
    * `http://localhost:8080/apis?fileName={fileName}`でSwaggerを読み込みリストを作成
    * `http://localhost:8080/apis/{apiName}`で選択したAPIのリクエストパラメータ一覧を表示
    * `http://localhost:8080/send`でAPIの実行

`form`パッケージ
 * `ApiTesterForm` ... フォームクラス。セッション保持用

`model`パッケージ

主要オブジェクト
 * `ApiTelegram` ... API(HTTPメソッド、リクエストパス）単位に設定する各種情報のルートオブジェクト
   * `ApiRequestParameter` ... APIの実行に必要なリクエストパラメータ情報
 * `ApiTesterResponse` ... ApiTesterがAPIを実行した結果を格納するオブジェクト

その他
 * `ApiTelegramBuilder` ... `ApiTelegram`のビルダー
 * `ApiRequestProperty` ... Swaggerの各プロパティを統一的に扱うオブジェクト
 
 `svc`パッケージ 
 * `ApiRequestSender` ... APIを実行するクラス
 * `OasAnalyzer` ... Swaggerを解析し`ApiTelegram`を作成するクラス
 * `OasFileSearchService` ... Swaggerを検索するクラス

ビュー
  * `/resources/templates/index.html`
  
#### 直したいところ

1. swagger-coreのモデルを活かしきれてない気がする

    できることならswagger-coreのPropertyやParameterをそのまま扱いたいが良い方法が思い浮かばない・・・

1. リクエストのJsonパラメータ作成時、オブジェクトや配列の構造を再現するために文字列解析をしている

    Swaggerの解析時に"::"を使って構造を定義している。
    オブジェクトならobject::stringやobject::integer、配列ならarray::stringやarray::object::string
    リクエストパラメータは平で持っており構造情報を持っていない。`ApiRequestParameter`に構造を持たせれば良い？

1. 配列のフィールドが1つしか持てない

    get(0)で決めうちしている。上記の文字列解析のせい。構造化すれば解決する？
