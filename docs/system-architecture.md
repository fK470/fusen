# Fusen アプリケーション システム構成図

## 1. 現在の開発環境構成

```mermaid
graph TB
    subgraph "ローカル開発環境"
        subgraph "フロントエンド (localhost:5173)"
            FE[React + TypeScript + Vite]
            FE_PROXY[Vite Proxy /api]
        end
        
        subgraph "バックエンド (localhost:8080)"
            BE[Spring Boot + Doma ORM]
            BE_API[REST API /api/v1]
        end
        
        subgraph "データベース (localhost:3306)"
            DB[(MySQL 8.0)]
            MIGRATION[Flyway Migration]
        end
    end
    
    USER[👤 開発者] --> FE
    FE --> FE_PROXY
    FE_PROXY --> BE_API
    BE --> DB
    MIGRATION --> DB
```

## 2. 目標：個人利用本番環境構成（Vercel + AWS）

```mermaid
graph TB
    subgraph "インターネット"
        USER[👤 個人ユーザー]
        DOMAIN_FRONT[fusen-app.vercel.app]
        DOMAIN_API[*.amazonaws.com]
    end
    
    subgraph "Vercel (フロントエンド) - 無料プラン"
        subgraph "Vercel Platform"
            VERCEL_BUILD[自動ビルド & デプロイ]
            VERCEL_APP[React SPA]
        end
    end
    
    subgraph "AWS (バックエンド) - 最小構成"
        subgraph "ECS Fargate"
            ECS_SERVICE[ECS Service]
            ECS_TASK[Spring Boot Container]
        end
        
        subgraph "RDS MySQL"
            RDS[(RDS Single-AZ)]
            RDS_BACKUP[(自動バックアップ 7日)]
        end
        
        subgraph "Security & Monitoring"
            SG[Security Groups]
            CW[CloudWatch Logs (基本)]
        end
    end
    
    subgraph "CI/CD"
        GITHUB[GitHub Repository]
        GITHUB_ACTIONS[GitHub Actions]
        ECR[Amazon ECR]
    end
    
    %% User Flow
    USER --> DOMAIN_FRONT
    USER --> DOMAIN_API
    
    %% Frontend Flow
    DOMAIN_FRONT --> VERCEL_CDN
    VERCEL_CDN --> VERCEL_APP
    GITHUB --> VERCEL_BUILD
    VERCEL_BUILD --> VERCEL_APP
    
    %% Backend Flow
    DOMAIN_API --> DNS
    DNS --> ALB
    ALB --> ECS_SERVICE
    ECS_SERVICE --> ECS_TASK1
    ECS_SERVICE --> ECS_TASK2
    ECS_TASK1 --> RDS
    ECS_TASK2 --> RDS
    
    %% API Communication
    VERCEL_APP -.->|HTTPS API Calls| ALB
    
    %% CI/CD Flow
    GITHUB --> GITHUB_ACTIONS
    GITHUB_ACTIONS --> ECR
    ECR --> ECS_SERVICE
    
    %% Security & Config
    SSM --> ECS_TASK1
    SSM --> ECS_TASK2
    SG --> ALB
    SG --> ECS_SERVICE
    SG --> RDS
    CW --> ECS_TASK1
    CW --> ECS_TASK2
    RDS --> RDS_BACKUP
```

## 3. データフロー図

```mermaid
sequenceDiagram
    participant U as ユーザー
    participant V as Vercel (React)
    participant CF as CloudFront/CDN
    participant ALB as AWS ALB
    participant ECS as ECS Fargate
    participant RDS as RDS MySQL
    
    Note over U,RDS: ページ初回ロード
    U->>+V: https://fusen-app.com アクセス
    V->>+CF: 静的ファイル配信
    CF-->>-U: React SPA配信
    
    Note over U,RDS: API呼び出し（ブックマーク一覧取得）
    U->>+V: ブックマーク一覧要求
    V->>+ALB: GET /api/v1/bookmarks
    ALB->>+ECS: リクエスト転送
    ECS->>+RDS: SELECT * FROM bookmarks
    RDS-->>-ECS: ブックマークデータ
    ECS-->>-ALB: JSON レスポンス
    ALB-->>-V: HTTP 200 + JSON
    V-->>-U: ブックマーク一覧表示
    
    Note over U,RDS: 新規ブックマーク作成
    U->>+V: 新規作成フォーム送信
    V->>+ALB: POST /api/v1/bookmarks
    ALB->>+ECS: リクエスト転送
    ECS->>+RDS: INSERT INTO bookmarks
    RDS-->>-ECS: 作成完了
    ECS-->>-ALB: HTTP 201 + JSON
    ALB-->>-V: 作成成功
    V-->>-U: 成功メッセージ + リスト更新
```

## 4. セキュリティ構成

```mermaid
graph TB
    subgraph "セキュリティ層"
        subgraph "Vercel Security"
            VERCEL_WAF[Web Application Firewall]
            VERCEL_DDoS[DDoS Protection]
            VERCEL_SSL[Auto SSL/HTTPS]
        end
        
        subgraph "AWS Security"
            WAF[AWS WAF]
            SHIELD[AWS Shield]
            
            subgraph "Network Security"
                VPC[VPC + Private Subnets]
                SG_ALB[ALB Security Group<br/>Port 80,443 from Internet]
                SG_ECS[ECS Security Group<br/>Port 8080 from ALB only]
                SG_RDS[RDS Security Group<br/>Port 3306 from ECS only]
                NACL[Network ACLs]
            end
            
            subgraph "Data Security"
                RDS_ENCRYPT[RDS Encryption at Rest]
                RDS_TLS[TLS in Transit]
                SSM_ENCRYPT[Parameter Store Encryption]
            end
            
            subgraph "Access Control"
                IAM[IAM Roles & Policies]
                ECS_TASK_ROLE[ECS Task Role]
                ECS_EXEC_ROLE[ECS Execution Role]
            end
        end
    end
    
    %% Security Flow
    VERCEL_WAF --> WAF
    VERCEL_DDoS --> SHIELD
    VERCEL_SSL --> VPC
    
    SG_ALB --> SG_ECS
    SG_ECS --> SG_RDS
    
    IAM --> ECS_TASK_ROLE
    IAM --> ECS_EXEC_ROLE
    ECS_TASK_ROLE --> SSM_ENCRYPT
    ECS_EXEC_ROLE --> RDS_ENCRYPT
```

## 5. 環境別構成（個人利用）

| 環境 | フロントエンド | バックエンド | データベース | 用途 |
|------|----------------|--------------|--------------|------|
| **開発** | localhost:5173 | localhost:8080 | localhost:3306 (Docker) | ローカル開発 |
| **本番** | Vercel (無料) | AWS ECS Fargate | RDS t3.micro | 個人利用・ポートフォリオ |

**個人利用のため、ステージング環境は省略してコストを最小化**

## 6. 技術スタック詳細

### フロントエンド
- **Framework**: React 19 + TypeScript
- **Build Tool**: Vite 6.3
- **Styling**: Tailwind CSS 4.1
- **HTTP Client**: Axios
- **Routing**: React Router DOM 7.6
- **Testing**: Vitest + Testing Library

### バックエンド  
- **Framework**: Spring Boot 3.4.6
- **Language**: Java 21
- **ORM**: Doma 2.53
- **Database**: MySQL 8.0
- **Build Tool**: Maven 3.9
- **Container**: Docker + Amazon Corretto 21

### インフラストラクチャ
- **Frontend Hosting**: Vercel
- **Container Platform**: AWS ECS Fargate
- **Database**: AWS RDS MySQL
- **Load Balancer**: AWS Application Load Balancer
- **DNS**: AWS Route 53
- **CI/CD**: GitHub Actions
- **Container Registry**: Amazon ECR
- **Monitoring**: AWS CloudWatch
- **Secrets Management**: AWS Parameter Store

## 7. デプロイフロー

```mermaid
graph LR
    subgraph "開発フロー"
        DEV[ローカル開発] --> PR[Pull Request]
        PR --> REVIEW[コードレビュー]
        REVIEW --> MERGE[main ブランチマージ]
    end
    
    subgraph "自動デプロイ"
        MERGE --> TRIGGER[GitHub Actions トリガー]
        
        subgraph "フロントエンド"
            TRIGGER --> VERCEL_BUILD[Vercel Build]
            VERCEL_BUILD --> VERCEL_DEPLOY[Vercel Deploy]
        end
        
        subgraph "バックエンド"
            TRIGGER --> BUILD[Docker Build]
            BUILD --> ECR_PUSH[ECR Push]
            ECR_PUSH --> ECS_DEPLOY[ECS Service Update]
        end
    end
    
    subgraph "検証"
        VERCEL_DEPLOY --> HEALTH_CHECK[ヘルスチェック]
        ECS_DEPLOY --> HEALTH_CHECK
        HEALTH_CHECK --> NOTIFICATION[Slack 通知]
    end
```

---

**このシステム構成図により、以下が明確になります:**

1. **開発環境から本番環境への移行計画**
2. **セキュリティ要件と実装方法**  
3. **スケーラビリティとパフォーマンス考慮点**
4. **運用・監視の仕組み**
5. **CI/CDパイプライン設計**

次のステップとして、この構成に基づいた具体的な実装作業に入りましょうか？