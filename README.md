# DAMLN-BoostSRL

This project is based on https://github.com/starling-lab/BoostSRL. MLN mode is extended to DA-MLN (Domain-size Aware Markov Logic Networks Mittal et al. (2019))  

Train mode saves additional files containing information about domain sizes. Test mode has additional flag `-dss` that activates domain size scaling.  
Only works with clause-based MLN (`-mln -mlnClause`).  
`Sample` project contains `IMDB` and `WebKB` datasets and tools generating folds and sampling. 

### WebKB Example Configrutaions
Train `java -jar DAMLNBoost.jar -mln -mlnClause -trees 10 -l -train datasets\MyWebKB\train\ -target faculty`  
Test `java -jar DAMLNBoost.jar -mln -mlnClause -trees 10 -i -test datasets\MyWebKB\test\ -model datasets\MyWebKB\train\models\ -target faculty`  
Test with domain size scaling `java -jar DAMLNBoost.jar -mln -mlnClause -dss -trees 10 -i -test datasets\MyWebKB\test\ -model datasets\MyWebKB\train\models\ -target faculty`  

### WebKB Example Folds and Sampling
Create 3 cross validation folds `java -jar WebKBFolds.jar -foldsFolder "datasets/mywebkb/folds/" -dbFile "db.texas.txt" -folds 3`  
Sample 20% and create train dataset from first fold `java -jar WebKBSample.jar -dataset "datasets/mywebkb/" -foldsFolder "datasets/mywebkb/folds/" -folds 3 -trainFold 1 -sample 0.2`


### Project Notes
Folder `src/edu/wisc/cs/will/FOPC_MLN_ILP_Parser` contains `.fopcLibrary` files. These need to be present with same path in `target` folder with compiled java class files. You can just copy them once after first build.  
To build executable jar for BoostSRL use Maven tool window `Maven -> Lifecycle -> package`  
To build executable jar for Sample tools use IntelliJ `Build -> Build Artifacts`