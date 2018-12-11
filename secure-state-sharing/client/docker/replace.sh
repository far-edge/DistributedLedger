#!/bin/bash
 if [ $# -ne 3 ]; then
   echo "USAGE:
         ./$0 template.txt destination.txt variables.txt"
   exit 1
 fi
echo "#!/bin/bash
cat > $2 << EOF
`cat $1`
EOF " > $2;
chmod +x $2;
# load variables into env
. $3
# actually replace variables
./$2