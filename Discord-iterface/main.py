from dotenv import load_dotenv
from config.serverConfig import run_server

def main():
    load_dotenv()
    run_server()

if __name__ == "__main__":
    main()
