##
## Takes mu and sigma vectors as gaussians for 
## classical image segmentation along with raster
## that is (MxN) matrix that contains pixel intensity
## returns (MxN) matrix of labels
##
## Author: Pavel SENIN
##
## classes - vector first column classes means
##                  second column classes standard deviations
##
## raster  - M x N matrix of image, each value from 0 to 1 
##                                 reports color intensity
##
## returns - M x N matrix of labels for each pixel in the raster
##
##
segmentImage <- function(classes, raster){

 # extract dimension data at first
 size_x <-dim(raster)[1]
 size_y <-dim(raster)[2]

 pixel_num <- size_x*size_y
 classes_dim <- dim(classes)[1]

 # initialize segmentation and temporarily segmentation
 segmentation <- matrix(rep(0,pixel_num),nrow=size_x, ncol=size_y, byrow=TRUE)
 segmentation_t <- segmentation
 
 # assign label to the each of pixels
 for(i in 1:classes_dim){
  #get mean and st.dev
  c_mu <- classes[i,1];  c_s <- classes[i,2]
  #get p values for each of the pixels from the raster
  segmentation_draft <- dnorm(raster, c_mu, c_s)
  #if some new values greater than previous - assign index
  segmentation[segmentation_t[,] < segmentation_draft[,]] <- i
  # save maiximal so far values in segmentation_t
  for(k in 1:size_x){
   for(l in 1:size_y){
    if(segmentation_t[k,l] < segmentation_draft[k,l]) {
      segmentation_t[k,l] <- segmentation_draft[k,l]
    }
   }
  }
 }
 # return labels
 segmentation
}

singleton <- function(raster_value, mean, variance)
{
  log(sqrt(2.0*3.141592653589793*variance))+
    ((raster_value-mean)^2)/(2.0*variance)
}


doubleton <- function(i, j, raster, labels, beta)
{
  energy <- 0.0;
  height <- dim(raster)[1]
  width <- dim(raster)[2]
  label <- labels[i,j]

  if (i!=height) # south
    {
      if (label == labels[i+1,j]) energy <- energy - beta
      else energy <- energy + beta
    }
  if (j!=width) # east
    {
      if (label == labels[i,j+1]) energy <- energy - beta
      else energy <- energy + beta
    }
  if (i!=1) # nord
    {
      if (label == labels[i-1,j]) energy <- energy - beta
      else energy <- energy + beta
    }
  if (j!=1) # west
    {
      if (label == labels[i,j-1]) energy <- energy - beta
      else energy <- energy + beta
    }
  energy
}

local_energy <- function(i, j, label, raster, labels, classes, beta)
{
  ## singleton singleton <- function(raster_value, mean, variance)
  ## doubleton doubleton <- function(i, j, label, raster, labels, beta)
  singleton(raster[i,j], classes[label,1], classes[label,2]) + doubleton(i,j,raster,labels,beta)
}

energy <- function(raster, labels, classes, beta)
{
  singletons <- 0.0;
  doubletons <- 0.0;
  height <- dim(raster)[1]
  width <- dim(raster)[2]
  
  for (i in 1:height){
    for (j in 1:width){
        k<-labels[i,j]
	  ## singleton singleton <- function(raster_value, mean, variance)
	  singletons <- singletons + singleton(raster[i,j], classes[k,1], classes[k,2])
	  ## doubleton doubleton <- function(i, j, label, raster, labels, beta)
	  doubletons <- doubletons + doubleton(i,j,raster,labels,beta)
      }
  }    
  singletons + doubletons / 2
}

##
## This is a Gibbs sampler for sampling parameter OMEGA, i.e. define classes
##
omegaGibbs <- function(raster, labels, classes, beta)
{
 # get dimensions
 width <-dim(raster)[1]
 height <-dim(raster)[2]

 classes_dim <- dim(classes)[1]
#  InitOutImage();
#  int i, j;
#  double *Ek;		       // array to store local energies
#  int s;
#  double summa_deltaE;
#  double sumE;
#  double z;
#  double r;

   t <- 50
   c <- 0.5

#  Ek = new double[no_regions];
   Ek <- matrix(rep(0,classes_dim))

#  K = 0;
   K <- 0
#  T = T0;
   T <- 4.0
#  E_old = CalculateEnergy();
   E_old <- energy(raster, labels, classes, beta)
   sum_deltaE <- 100
   
   while(sum_deltaE >t)  ## stop when energy change is small
   {
     sum_deltaE = 0.0;
     for(i in 1:height) {
	  for (j in 1:width) {
	    
          sumE = 0.0;
	    for(s in 1:classes_dim) {
		 Ek[s] <- exp(-local_energy(i, j, s, raster, labels, classes, beta) / T)
		 sumE <- sumE + Ek[s]
	    }
	    
	    r <- runif(1)
	    z <- 0.0;

	    for(s in 1:classes_dim) {
 		 z <- z + Ek[s] / sumE
             # choose new label with probabilty exp(-U/T)
             print(c(z,r,Ek[s],sumE))
		 if(z >= r) {
   	         labels[i,j] <- s
	         ##print(c(c("changed"),i,j,labels[i,j]))
		   break
		 }
	    }

	  } #for j...
	 } #for i...
	 
     E <- energy(raster, labels, classes, beta)
     print("new energy")
     print(E)
     sum_deltaE <- sum_deltaE + abs(E_old - E)
     print(sum_deltaE)
     E_old <- E

     T = T * c  # decrease temperature
     K <- K + 1 # advance iteration counter
    
    } ## while

  labels
}