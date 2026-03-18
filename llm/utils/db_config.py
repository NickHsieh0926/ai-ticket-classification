import os
from urllib.parse import quote_plus
from sqlalchemy import create_engine


def get_engine():
    password = quote_plus(os.getenv("DB_PASSWORD", ""))
    db_url = (
        f"postgresql://{os.getenv('DB_USERNAME', 'sa')}:"
        f"{password}@"
        f"{os.getenv('DB_HOST', 'postgres')}:"
        f"{os.getenv('DB_PORT', '5432')}/"
        f"{os.getenv('DB_NAME', 'ai_ticket')}"
    )
    return create_engine(db_url)
