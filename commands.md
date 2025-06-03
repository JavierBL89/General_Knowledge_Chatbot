list ports in used
 - lsof -i :7860

kill port
 - kill -9 <port>



1. Ensure you're in a virtual environment. If you haven't created one, create it:
   bash

    - python3.10 -m venv venv   (VERSION 10 NEEDED, not 13)

2. Activate the virtual environment:

    - source venv/bin/activate

   
5. Install Flask and all other required packages.
Verify Installation:
Check if Flask is installed:

- pip show flask

- pip install flask


7. Install Langchain:

   - pip install langchain-core langchain-community langchain
   

6. Install Dependencies from requirements.txt:
   Install all the dependencies listed in your requirements.txt file:

   - pip freeze > requirements.txt

   - pip install -r requirements.txt
