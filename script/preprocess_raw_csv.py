# ---
# jupyter:
#   jupytext:
#     text_representation:
#       extension: .py
#       format_name: percent
#       format_version: '1.3'
#       jupytext_version: 1.18.1
#   kernelspec:
#     display_name: Python (llm_env)
#     language: python
#     name: llm_env
# ---

# %%
import pandas as pd

# 讀原始 CSV
df = pd.read_csv("../data/raw/multilingual_customer_support_tickets.csv")

# 選英文工單
df = df[df["language"] == "en"]

# 合併 subject + body
df["ticket_text"] = df["subject"].fillna('') + " " + df["body"].fillna('')

# 選擇第一個標籤作為 category
df["category"] = df["tag_1"]

# 去掉缺 category 的列
df = df.dropna(subset=["category", "ticket_text"])

# 加上 ticket_id
df["ticket_id"] = range(len(df))

# 只保留需要欄位
df_tickets = df[["ticket_id", "ticket_text", "category"]]

# 存成新的 CSV（放到 processed 資料夾）
df_tickets.to_csv("../data/processed/tickets.csv", index=False)

print("✅ tickets.csv 已生成於 ../data/processed/")


# %%
