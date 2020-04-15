### Approach
#### Index
Theory on different similarity method
Theory on Preprocessing (different analyzer)
#### Query
underlying information extraction
* Binary relevance judgments with specified booster
* extract information from narrative

query expansion
* based on synonyms
* based on results (Rocchio)
### Implementation
#### Index
Judge and extract useful fields (store index for later query expansion)
different filter and similarity method in preprocessing implementation(self-defined stop words )
#### Query
binary relevance judgments
* what fields are more important (decided by experiment)
* extract information from narrative (must field)
* why not neg field
* country name/ quote item are important

query expansion
* based on synonyms (what fields)  (why not in the index)
* based on results (Rocchio) 
    * how to use title and text seperately

#### Result
Difference trends in map and P@N
compare
* all synonyms
* some synonyms 
* without synonyms

compare
* with/ without country

compare
* terms/ topic

compare
* with/ without neg fields

with more expansion,