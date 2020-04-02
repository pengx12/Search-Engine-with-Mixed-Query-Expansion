#### SSH
    ssh -i "irtry.pem" ubuntu@ec2-34-238-172-160.compute-1.amazonaws.com
#### Build
The project is located in directory ir_artifact/
    cd ir_artifact/
    mvn package
#### Run
    cd target
    java -jar ir_artifact-1.0-SNAPSHOT.jar
#### Evaluate
The result file called "results.out" is located in trec_eval directory/
    cd trec_eval 
    make
    ./trec_eval qrels.assignment2.part1 results.out 
