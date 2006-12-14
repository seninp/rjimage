library(rgdal)
library(pixmap)
#file="../data/grey_seagull41.png"
file="../data/seagull_segmented.png"
#file2="../data/seagull_icm.png"
#file3="../data/seagull_gibbs.png"
#file4="../data/seagull_metropolis.png"
#
image1 <- GDAL.open(file) 
image1.description <- getDescription(image1)
image1.driver <- getDriverLongName(getDriver(image1))
image1.dim <- dim(image1)
image1.metadata <- getMetadata(image1)
image1.raster <- getRasterData(image1, band = 1, offset = c(0, 0),dim(image1), dim(image1), interleave = c(0, 0), as.is = FALSE)
image1.raster <- image1.raster / 255
image1.raster.flat <- t(getRasterData(image1, band=1)) / 255
#displayDataset(image1, band=1, , image.dim = c(331, 500), reset = FALSE)
#title("grey_seagull41.png", sub="500 x 331")
plot(density(image1.raster), xlim=c(0,1), main = "", sub="", col="red", lwd=4)
abline(v=0.2, col="maroon",lwd=4)
abline(v=0.4, col="khaki",lwd=4)
abline(v=0.6, col="violet",lwd=4)
abline(v=0.8, col="cyan",lwd=4)
g1<-function(x) dnorm(x,0.2,0.2)*3
curve(g1,0,1,col="maroon",lwd=4,add=TRUE)
g1<-function(x) dnorm(x,0.4,0.2)*3
curve(g1,0,1,col="khaki",lwd=4,add=TRUE)
g1<-function(x) dnorm(x,0.6,0.2)*3
curve(g1,0,1,col="violet",lwd=4,add=TRUE)
g1<-function(x) dnorm(x,0.8,0.2)*3
curve(g1,0,1,col="cyan",lwd=4,add=TRUE)


GDAL.close(image1)
image2 <- GDAL.open(file2) 
image2.description <- getDescription(image2)
image2.driver <- getDriverLongName(getDriver(image2))
image2.dim <- dim(image2)
image2.metadata <- getMetadata(image2)
image2.raster <- getRasterData(image2, band = 1, offset = c(0, 0),dim(image2), dim(image2), interleave = c(0, 0), as.is = FALSE)
image2.raster <- image2.raster / 255
displayDataset(image2, band=1, , image.dim = c(334, 500), reset = FALSE)
title("original image", sub="500 x 334")
plot(density(image2.raster), xlim=c(0,1), main = "Pixel's density", sub="pre-defined classes")
abline(v=0.2, col="red")
abline(v=0.4, col="red")
abline(v=0.6, col="red")
abline(v=0.8, col="red")