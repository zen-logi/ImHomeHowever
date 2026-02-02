# ImHomeHowever

帰宅時の電車経路を Discord に通知する Bot

## 機能

- `/imhome from:[出発駅] to:[到着駅] via:[経由駅]` コマンドで経路検索
- Yahoo 路線情報からスクレイピングで取得
- AWS Lambda + SQS でサーバーレス運用

## アーキテクチャ

```
Discord
   │
   ▼ (Webhook)
Lambda: ImHomeBot ──► SQS Queue ──► Lambda: ImHomeWorker
   │                                    │
   ▼                                    ▼
type=5 Deferred ACK              Yahoo 路線情報スクレイピング
                                        │
                                        ▼
                                   Discord Follow-up
```

## 技術スタック

| カテゴリ | 技術 |
|----------|------|
| 言語 | Kotlin 2.0 |
| フレームワーク | Ktor 3.0 |
| DI | Koin |
| Discord API | JDA 6 |
| スクレイピング | Jsoup |
| デプロイ | AWS Lambda (Container Image) |
| キュー | AWS SQS |

## セットアップ

### 必要なもの

- JDK 21+
- Docker
- AWS CLI（設定済み）
- Discord Bot Token

### 環境変数

| 変数名 | 説明 |
|--------|------|
| `DISCORD_TOKEN` | Discord Bot トークン |
| `PUBLIC_KEY` | Discord Application 公開鍵 |
| `CHANNEL_ID` | 通知先チャンネル ID |
| `GUILD_ID` | スラッシュコマンド登録先ギルド ID |
| `QUEUE_URL` | SQS キュー URL |

### ローカル開発

```bash
# ビルド
./gradlew shadowJar

# ローカル実行
./gradlew run
```

### AWS Lambda へのデプロイ

```bash
# Docker イメージビルド & プッシュ
docker build --platform linux/amd64 -t im-home-however:latest .
docker tag im-home-however:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/im-home-however:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/im-home-however:latest

# Lambda 更新
aws lambda update-function-code --function-name ImHomeBot --image-uri <IMAGE_URI>
aws lambda update-function-code --function-name ImHomeWorker --image-uri <IMAGE_URI>
```

## Discord 設定

1. [Discord Developer Portal](https://discord.com/developers/applications) でアプリケーション作成
2. Bot を作成してトークンを取得
3. **Interactions Endpoint URL** に `https://<LAMBDA_FUNCTION_URL>/api/discord/webhook` を設定
4. `applications.commands` スコープでサーバーに Bot を追加

## ライセンス

MIT
