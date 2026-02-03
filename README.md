# AI-Ticket-Classifier: AI工單分類系統

本專案是一個全棧式 AI工單分類系統，從數據科學訓練模擬 (Python/Jupyter) 走向生產級架構 (Java Spring Boot/FastAPI)。在 V1.1 版本中，引入了**分散式追蹤、非同步高併發優化與 WebSocket 即時通訊**，旨在解決大規模工單處理中的效能瓶頸與除錯效率。

---

## 系統架構演進

### **V1.0：基礎功能實作**

- **AI Engine**: 基於 Scikit-learn 的 NLP 分類流水線（TF-IDF + LR）。
- **Serving**: FastAPI 封裝模型 API，實作 Python/Java 跨系統通訊。
- **Workflow**: 前端發送請求 -> Java 轉發 Python -> 返回結果 -> 前端渲染。

### **V1.1：架構升級**

- **全鏈路日誌** : 實作跨服務 **TraceID (MDC)**，串接 Java 至 Python 的所有 Log。
- **非同步架構** : 引入 `ThreadPoolExecutor` 與 `TaskDecorator`，解決 AI 推論阻塞問題並修正執行緒切換間的上下文遺失。
- **即時反饋 (UX)**: 整合 **WebSocket**，實現「非同步處理、即時推送」。

---

## 技術棧

| **領域** | **技術選型** |
| --- | --- |
| **AI / Serving** | Python 3.10, FastAPI, Scikit-learn, Pandas, Joblib |
| **Backend** | Java 17, Spring Boot 4.x, **Spring Security (JWT)**, PostgreSQL |
| **Frontend** | **Nuxt 4**, Vue 3, Tailwind CSS, Pinia, EChart.js |
| **Infrastructure** | Docker, Docker Compose, Log4j2 (MDC) |

---

## 核心技術實作

### 1. 全鏈路 MDC 追蹤

### 2. 非同步併發控制

### 3. WebSocket 批次處理監控

---

## 視覺化展示 

- **智能儀表板**: 實時呈現工單分類分布 (Bar Chart) 與模型信心度分布。
- **批量操作**: 支援數百筆工單一鍵分類，並透過 TraceID 進行批次追蹤。
- **安全性**: 全 API 接口受 JWT 保護，確保數據存取權限。

---

## 系統架構設計 

### 時序圖
![Sequence Diagram](./UML/images/Upload_Sequence_Diagram.jpg)

### 類別圖
![Class Diagram](./UML/images/Upload_Class_Diagram.jpg)

---

## 📦 快速啟動 

### 前置需求

- Docker & Docker Compose
- WSL
- Java 17 (本地開發用) / Python 3.10 (本地開發用)

### 部署步驟

1. **clone 專案**
    
    `git clone https://github.com/NickHsieh0926/ai-ticket-classification.git`
    
2. **一鍵啟動 (Docker Compose)**
    
    `docker-compose up --build -d`