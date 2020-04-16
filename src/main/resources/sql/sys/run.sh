POSTGRES_USER="skyplus"
POSTGRES_PASSWORD="skyplus"
DATABASE_NAME="skyplus"

if [[ $# -eq 0 ]] ; then
    echo 'Please enter the folder where you want to import all *.sql files under it.'
    exit 0
fi


echo "Importing..."

export PGPASSWORD=$POSTGRES_PASSWORD

for file in `find $1 | grep -i '.sql'` 
do 
  echo "importing $file"
  psql -U $POSTGRES_USER -h localhost -d $DATABASE_NAME < $file
done
