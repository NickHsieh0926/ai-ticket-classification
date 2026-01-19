import logging
import os
from logging.handlers import TimedRotatingFileHandler
from contextvars import ContextVar


trace_id_var = ContextVar("trace_id", default="n/a")
span_id_var = ContextVar("span_id", default="n/a")


class TraceIDFilter(logging.Filter):
    def filter(self, record):
        record.trace_id = trace_id_var.get()
        record.span_id = span_id_var.get()
        return True


def setup_logger():
    current_file_path = os.path.abspath(__file__)
    utils_dir = os.path.dirname(current_file_path)
    project_root = os.path.dirname(utils_dir)
    log_dir = os.path.join(project_root, "logs")

    if not os.path.exists(log_dir):
        os.makedirs(log_dir)

    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    if logger.hasHandlers():
        logger.handlers.clear()

    formatter = logging.Formatter(
        "%(asctime)s [%(levelname)s] [TraceID: %(trace_id)s] [SpanID: %(span_id)s] %(name)s - %(message)s"
    )

    trace_filter = TraceIDFilter()

    # 1. 終端機 Handler
    console_handler = logging.StreamHandler()
    console_handler.addFilter(trace_filter)  
    console_handler.setFormatter(formatter)

    # 2. 檔案 Handler
    file_handler = TimedRotatingFileHandler(
        filename=os.path.join(log_dir, "python-ai.log"),
        when="midnight",
        interval=1,
        backupCount=30,
        encoding="utf-8",
    )
    file_handler.addFilter(trace_filter)  
    file_handler.setFormatter(formatter)
    file_handler.suffix = "%Y-%m-%d.log"

    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

    return logger

setup_logger()
