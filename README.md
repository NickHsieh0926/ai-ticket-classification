# AI Ticket Classification System

這是一個全棧式的 AI 落地解決方案，涵蓋了從**數據探索、模型訓練、模型服務化 (FastAPI)**，到**後端業務邏輯串接 (Java Spring Boot)**，以及**前端數據視覺化 (Nuxt 4)** 的完整流程。

## 專案核心架構

本專案分為四個主要層次：

1. **AI Engine**: 基於 Python 的機器學習模型，處理文本分類。
2. **AI Service**: 使用 FastAPI 封裝模型，提供高效的 REST API。
3. **Backend Logic**: Java Spring Boot 負責業務邏輯。
4. **Frontend Dashboard**: Nuxt 4 (Vue 3) 提供直觀的分類分布與模型信心度視覺化。

---

## 🛠 實作階段 (Phases)

### Phase 1: 模型開發 (Data Science)

- **Data Exploration**: 深入分析 Ticket 數據分布。
- **Baseline Model**: 建立 TF-IDF + Logistic Regression 的基準模型。
- **Optimization**: 進行多模型比較，最終選擇最佳模型並進行持久化 (Persistence)。

### Phase 2: 模型工程化 (AI Engineering)

- **Refactoring**: 將 Jupyter Notebook 代碼轉化為標準 Python 模組。
- **Inference Pipeline**: 標準化推論流程，確保訓練與預測的一致性。
- **FastAPI**: 封裝推論邏輯，提供 `/predict` 接口。

### Phase 3: 業務落地 (Backend)

- **Java Integration**: Spring Boot 通過 RestTemplate/WebClient 串接 AI 服務，將 AI 能力導入現有業務流程。

### Phase 4: 數據視覺化 (Frontend)

- **Nuxt 4 Framework**: 採用最新 Nuxt 4 架構與 Pinia 狀態管理。
- **Features**:
    - **Dashboard**: 分類分布 BarChart、信心度 (Confidence) 分布直方圖。
    - **Data Table**: 支援分頁 (Pagination)、篩選 (Filter) 及批量預測。
    - **Infrastructure**: 處理 Docker 環境下的 Vite Proxy 與 CORS 議題。

### Phase 5: 部署一致性 (DevOps)

- **Dockerization**: 為 FastAPI、Java、Nuxt 服務撰寫 Dockerfile。
- **Docker Compose**: 實現一鍵啟動全環境（Java + Python + Frontend）。

---

## 📦 技術棧

- **AI**: Python, Scikit-learn, Pandas, Joblib
- **Serving**: FastAPI, Uvicorn
- **Backend**: Java 17+, Spring Boot 3.x
- **Frontend**: Nuxt 4, Vue 3, Tailwind CSS, Pinia, Chart.js
- **Infrastructure**: Docker, Docker Compose

---

## 🚦 快速啟動

### 前置需求

- Docker & WSL

### 部署步驟

1. **複製專案**Bash
    
    `git clone https://github.com/NickHsieh0926/ai-ticket-classification.git`
    
2. **一鍵啟動**Bash
    
    `docker-compose up --build`