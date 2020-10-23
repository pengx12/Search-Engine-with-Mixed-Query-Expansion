# News-Search-App
A search Engine based on Apache Lucene. The content in question is an aggregated collection of news articles (mostly) from a number of sources: the Financial Times Limited (1991, 1992, 1993, 1994), the Federal Register (1994), the Foreign Broadcast Information Service (1996) and the Los Angeles Times (1989, 1990).
Achieved 0.293 map with the whole QRels file. Won the first prize among 13 teams.

## Running App
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


