##
## loading specific library to process images
library(rgdal)
library(pixmap)
##
## include my functions code
source("moves.R")
##
## Initialize display to plot our stuff
	par(mfrow=c(3,3))
	##
	## open sample image and get raster data
	image1 <- GDAL.open("rcode/test1.PNG") 
	image1.description <- getDescription(image1)
	image1.driver <- getDriverLongName(getDriver(image1))
	image1.dim <- dim(image1)
	image1.metadata <- getMetadata(image1)
	image1.raster <- getRasterData(image1, band = 1, offset = c(0, 0),dim(image1), dim(image1), interleave = c(0, 0), as.is = FALSE)
	image1.raster <- image1.raster / 255
	image1.raster.flat <- t(getRasterData(image1, band=1)) / 255
	displayDataset(image1, band=1, reset = FALSE)
	GDAL.close(image1)
	##
	## plot image and raster density
	plot(density(image1.raster))
	##
	## Initialize simple Gaussian mixture
	beta<-0.28
	classes<-rbind(c(0.2, 0.1),c(0.4,0.1),c(0.5,0.1),c(0.6,0.1),c(0.7,0.1),c(0.8,0.1))
	labels <- segmentImage(classes, image1.raster)
	plot(pixmapIndexed(labels))
	labels <- omegaGibbs(image1.raster, labels, classes, beta)
	plot(pixmapIndexed(labels))
	labels <- omegaGibbs(image1.raster, labels, classes, beta)
	plot(pixmapIndexed(labels))
	labels <- omegaGibbs(image1.raster, labels, classes, beta)
	plot(pixmapIndexed(labels))


##
## Test the energy
beta<-0.28
nrg <- energy(image1.raster, labels, classes, beta)

###############################################################
###############################################################
## MOVE 1
segmentImage <- function(mu, sigma, raster){
}

plot(pixmapGrey(labels))
raster.beta <- 2.5
raster.bigK <- 200
raster.temperature <- 6.0
raster.image <- image1.raster.flat
raster.labels<- as.mtrix()
raster.labels_num <- 2
raster.weights <- as.vector(rep(0,raster.l))
raster.mus <- as.matrix(cbind( rep(0.2, raster.l) , rep(0.7, raster.l)))
matr.sigmas <- c(0.05, 0.00001, 0.05, 0.00001)
raster.sigma <-cbind( c(matr.sigma, matr.sigma))
##
## Testing section
##
doubleton(101,101,raster,labels,2.8)