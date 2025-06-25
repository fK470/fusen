# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

Fusenは、将来のマイクロサービスアーキテクチャを見据えたバックエンド開発学習を目的としたブックマーク管理アプリケーションです。プロジェクトはモノレポ構成で、バックエンド（Spring Boot）とフロントエンド（React）のアプリケーションが分離されています。

## 開発コマンド

### バックエンド（Spring Boot + Maven）
```bash
# /apps/backend ディレクトリから実行
mvn spring-boot:run                    # バックエンドサーバーを8080ポートで起動
mvn test                              # 全テスト実行
mvn clean compile                     # クリーン後コンパイル
mvn package                           # JARファイルビルド
```

### フロントエンド（React + Vite）
```bash
# /apps/frontend ディレクトリから実行
pnpm dev                              # 開発サーバー起動
pnpm build                            # 本番用ビルド
pnpm test                             # Vitestでテスト実行
pnpm lint                             # ESLint実行
```

### データベース & インフラ
```bash
# /apps ディレクトリから実行
docker-compose up db                  # MySQLデータベースのみ起動
docker-compose up                     # 全サービス起動（db + api）
docker-compose run flyway-migrate     # データベースマイグレーション実行
docker-compose run flyway-info        # マイグレーション状況確認
docker-compose run flyway-clean       # データベースクリーン（開発時のみ）
```

## アーキテクチャ & 主要パターン

### バックエンドアーキテクチャ（Spring Boot + Doma）
- **コントローラー層**: `/api/v1/bookmarks`でのREST エンドポイント、標準的なCRUD操作
- **サービス層**: `@Transactional`を使用したトランザクション管理付きビジネスロジック
- **DAO層**: Doma ORMを使用し、SQLファイルは`src/main/resources/META-INF/com/example/fusen/domain/repository/`に配置
- **エンティティクラス**: Lombokを使用したJPAエンティティでボイラープレートコード削減
- **例外処理**: カスタム例外（`BookmarkNotFoundException`、`DuplicateUrlException`、`InvalidUrlException`）とグローバル例外ハンドラー

### フロントエンドアーキテクチャ（React + TypeScript）
- **レスポンシブデザイン**: `useMediaQuery`フックを使用してモーダル（デスクトップ）とページ遷移（モバイル）のUXを切り替え
- **コンポーネント構造**: アトミックデザインによる`/atoms`、`/organisms`コンポーネント
- **API層**: `/api`ディレクトリのAxiosベースHTTPクライアントとTypeScriptインターフェース
- **状態管理**: データ同期用のリフレッシュトリガーを含むローカルReact状態

### データベーススキーマ
多対多関係を持つ3つの主要テーブル：
- `bookmarks`: コアブックマークデータ（id、url、title、description、タイムスタンプ）
- `tags`: タグ定義（id、name、タイムスタンプ）
- `bookmark_tags`: ブックマーク-タグ関係の中間テーブル

## 主要な開発パターン

### URL検証
バックエンドは `URI.create().toURL()` パターンでURL検証を実行。フロントエンドは検証エラーを適切に処理する必要があります。

### タグ管理
- タグはブックマーク保存時にオンデマンドで作成
- フロントエンドは文字列配列としてタグ名を送信、バックエンドでTagエンティティに変換
- 現在タグの独立削除は不可（MVP制限）

### エラーハンドリング
- バックエンドは意味のあるエラーコード付きカスタム例外を使用
- フロントエンドは標準HTTPステータスコード（400、404、500）を処理
- データベース制約違反はサービス層で処理

### レスポンシブUXパターン
フロントエンドは画面幅検出（`768px`ブレークポイント）で以下を決定：
- デスクトップ: 作成/編集操作でモーダルダイアログ
- モバイル: 作成/編集操作でフルページ遷移

## データベース接続
- ローカル開発はDocker MySQL（3306ポート）を使用
- データベース名: `fusen_local`
- 接続設定は`application.properties`と`flyway.conf`で構成
- スキーママイグレーションは`/apps/db/migrations/`でFlywayを使用

## テストアプローチ
- バックエンド: JUnit 5 + Mockito + AssertJ
- フロントエンド: Vitest + React Testing Library
- テストファイルは標準的なMaven/Vitestテストディレクトリに配置

## 重要な注意事項
- プロジェクトの一部UI要素とサンプルデータで日本語命名規則を使用
- Doma ORMはDAOパッケージパスに対応する特定のディレクトリ構造にSQLファイルが必要
- エンティティクラスでLombokを多用 - アノテーション処理が有効であることを確認
- 開発時のフロントエンド-バックエンド通信でCORSが設定済み

## 作業メモリ
- これまでの作業内容を整理し、必要な記憶事項を追加する予定
- 今後作業をするときは、issue作成 -> ブランチを切る -> 作業 -> PR作成の手順を必ず実施して
- 今後、PRのマージ前に必ずレビュー内容をチェックして、レビュー対応をしてからマージして