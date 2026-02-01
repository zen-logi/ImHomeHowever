# セキュリティポリシー

## 概要

このプロジェクトでは、AWS や Discord などの機密トークンやキーが誤ってコミットされることを防ぐため、複数のセキュリティ対策を実施しています。

## 自動セキュリティスキャン

### 実施内容

1. **Gitleaks による自動スキャン**
   - AWS アクセスキー、シークレットキー
   - Discord ボットトークン、Webhook URL
   - その他の API キーやシークレット

2. **TruffleHog による検証**
   - 検証済みのシークレット検出
   - Git 履歴全体のスキャン

### 実行タイミング

- **プッシュ時**: main/master/develop ブランチへのプッシュ
- **プルリクエスト**: 新規 PR 作成時および更新時
- **定期実行**: 毎日 9:00 JST（0:00 UTC）
- **手動実行**: GitHub Actions から手動でトリガー可能

## セキュリティベストプラクティス

### 1. 環境変数の使用

機密情報は**必ず**環境変数として管理してください：

```bash
# ✅ 正しい例
export DISCORD_TOKEN="your-token-here"
export AWS_ACCESS_KEY_ID="your-key-here"
```

```kotlin
// ✅ コードでは環境変数から取得
val token = System.getenv("DISCORD_TOKEN")
```

### 2. .gitignore の活用

以下のファイルは `.gitignore` で除外されています：

- `.env` および `.env.*` ファイル
- `*.pem`, `*.key` などの秘密鍵
- AWS credentials ファイル
- `secrets.yml`, `secrets.json` などのシークレットファイル

### 3. コミット前の確認

コミット前に以下を確認してください：

```bash
# Gitleaks でローカルスキャン（推奨）
docker run -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v

# 変更内容の確認
git diff
```

### 4. 誤ってコミットしてしまった場合

1. **即座に該当のトークン/キーを無効化・再生成**
2. Git 履歴から削除（BFG Repo-Cleaner や git filter-branch を使用）
3. 新しいトークン/キーを環境変数で設定

```bash
# 例: Discord トークンの再生成
# 1. Discord Developer Portal でトークンを再生成
# 2. Git 履歴から削除
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch path/to/file" \
  --prune-empty --tag-name-filter cat -- --all

# 3. 強制プッシュ（注意: チームメンバーと調整が必要）
git push origin --force --all
```

## セキュリティインシデント報告

セキュリティに関する問題を発見した場合：

1. **公開 Issue は作成しない**
2. リポジトリオーナーに直接連絡
3. 詳細な情報を提供（影響範囲、再現手順など）

## 定期的な監査

- GitHub Actions のスキャン結果を定期的に確認
- 依存関係の脆弱性チェック（Dependabot）
- アクセスキーやトークンの定期的なローテーション

## 参考リンク

- [Gitleaks 公式ドキュメント](https://github.com/gitleaks/gitleaks)
- [TruffleHog 公式ドキュメント](https://github.com/trufflesecurity/trufflehog)
- [AWS セキュリティベストプラクティス](https://aws.amazon.com/security/security-learning/)
- [Discord セキュリティガイド](https://discord.com/developers/docs/topics/oauth2)
