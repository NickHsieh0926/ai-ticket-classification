import re
import string
from typing import List
import nltk

# 下載 WordNet 資料（第一次用才需要）
nltk.download('wordnet')
nltk.download('omw-1.4')

from nltk.stem import WordNetLemmatizer

lemmatizer = WordNetLemmatizer()

# text preprocess
def clean_text(text: str) -> str:
    # 小寫化preprocessing.py
    text = text.lower()
    
    # 去掉標點符號
    text = text.translate(str.maketrans("", "", string.punctuation))
    
    # 去掉多餘空格
    text = re.sub(r'\s+', ' ', text).strip()
    
    # 分詞 + lemmatization
    words = text.split()
    words = [lemmatizer.lemmatize(word) for word in words]
    
    return ' '.join(words)

# 批次處理文字列表
def preprocess_corpus(corpus: List[str]) -> List[str]:
    return [clean_text(text) for text in corpus]

# 測試
if __name__ == "__main__":
    sample_texts = [
        "My internet is down, cannot connect to network.",
        "Billing issues! Need refund ASAP.",
        "Running faster than before."
    ]
    print(preprocess_corpus(sample_texts))