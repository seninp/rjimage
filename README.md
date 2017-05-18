# RJimage class project

The work follows publication by Z. Kato 
_Bayesian color image segmentation using reversible jump Markov chain Monte Carlo_, 
Probability, Networks and Algorithms, February, 28, 1999. CWI, Amsterdam.

## Build the package using Ant

`ant jar`

 - analyze.build.xml (a verify and reporting build target.) 
 - build.xml (contains basic build targets and property definitions) 
 - dist.build.xml (creates a distribution package) 
 - junit.build.xml (runs junit) 
 - emma.build.xml (runs emma) 
 - there may also be other *.build.xml files.&nbsp;

## Used libraries and other files :
 - checkstyle/checkstyle.modules.xml contains checkstyle customizations
 - pmd/pmd.rulesets.xml contains pmd customizations
 - findbugs/findbugs.rulesets.xml contains findbugs customizations
 - commons/commons-math-1.1.jar contains Jakarta Common Math library
 - swingmiglayout/swingmiglayout15.jar contains SWING MIG layout library for GUI buildant jar&quot;</p>

## Running
`java -jar rjimage-demo.jar`
