docker build -f deployment/Dockerfile -t budgetbook:1.0 .

docker run -v %cd%/deployment/mnt:/mnt/budget-book -p 80:80 budgetbook:1.0 

docker image save -o deployment/images/budget-book-1-0.tar budgetbook:1.0