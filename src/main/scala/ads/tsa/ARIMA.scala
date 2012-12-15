package ads.tsa

import scalation.analytics._
import scalation.linalgebra.{MatrixD, VectorD}
import scalation.plot.Plot
import scalation.random.Random
import scalation.util.Error
import scalation.stat.StatVector

/** This class provide basic time series analysis capabilities for
 *  Auto-Regressive (AR), Integrated (I) and Moving Average (MA) models.
 *  In an ARIMA(p, d, q) model, p, d and q refer to the order of the
 *  Auto-Regressive, Integrated and Moving Average components of the model. 
 *  ARIMA models are often used for forecasting.
 *  @param y  the input vector
 *  @param t  the time vector
 */
class ARIMA (y: VectorD, t: VectorD) extends Predictor with Error {

    // size of the input vector
    private val n = y.dim 

    /** Return a vector that is the Moving Average (MA) of the given vector.
     *  @param k  the number of points to average
     */
    def ma (k: Int): VectorD =
    {
        val kk = k.toDouble
        val z = new VectorD (n - k)
        for (i <- 0 until n - k) z(i) = y(i until i + k).sum / kk
        z
    } // ma

    /** Return a vector that is the Auto-Regressive (AR) predictions of the last k
     *  points.
     *  @param rho  the vector of auto-correlations
     */
    def ar (rho: VectorD): VectorD =
    {
        val k = rho.dim               // use lag-1 to lag-k
        val z = new VectorD (n - k)
        for (i <- k until n) {
            var sum = 0.
            for (j <- 0 until k) sum += rho(j) * y(i - j)
            z(i - k) = sum
        } // for
        z
    } // ar

    /** Returns the backwards differenc of the input vector
     *  @param f input vector
     *  @param h the lag
     */
    def bdiff (f: VectorD, h: Int): VectorD = {
        val vec = new VectorD (f.dim - h) 
        for (i <- h until f.dim) {
            vec(i - h) = f(i) - f(i - h)
	} // for
        vec
    } // bdiff

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Fit an ARIMA model to historical times series data.
     */
    def train ()
    {
         //We need to fit the data in vector y to the following function:

         //     (1- (sum_{1 to i}(AR_params_i*y[i-1]) = (1 + (sum_of_all(MA_params * lag_operator))) * error

         //The key is to find the parameters: AR_params and MA_params, denoted phi and theta from here forward
         //This proceeeds in three stages:
         // 1: Ensure that the vector y is stationary, or perform differencing until it is
         // 2: Find the order of AR <- p; and MA <- q
         // 3: Estimate the parameters: phi and theta
         // 4: Verfiy goodness of fit and repeat if necessary

         //STAGE 1: Detect Stationarity and Perform Differencing until the data is stationary
         // the number of differences = d, the order of integration
         //Threshhold for determining convergence
         val threshold = 5.0E-5
         var iter      = 0       
  
         var result:VectorD      = null
         var yt: VectorD         = null
         var r: SimpleRegression = null

         // Box-Jenkins using the Dickey–Fuller test
         // @see http://en.wikipedia.org/wiki/Box-Jenkins
         // @see http://en.wikipedia.org/wiki/Dickey%E2%80%93Fuller_test
         do {

             // increase iter
             iter += 1

             // get our backwards difference
             yt = bdiff(y, iter)
            
             // pad our backwards difference for regression
             val padded = new VectorD(Array.fill(iter)(1.0)) ++ yt

             // build our time matrix
             val x = new MatrixD (t.dim, 2)
             for (i <- 0 until t.dim) {
                 x(i, 0) = 1.0
                 x(i, 1) = t(i)
	           } // for
             println("before")

             // regression on x and padded yt
             r = new SimpleRegression(x, padded)
             println("after")
             r.train

             println("made it here! iter = %d".format(iter))

         } while (r.fit._2 > threshold && iter < 2)

         println("order of integration = %d".format(iter))
       
         //For debugging, print the Autocorrelation function of the yt vector
         plotACF(acf(yt))
         plotPACF(pacf(yt))

         //Interpreting the autocorrelation function
         //from NIST handbook http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc446.htm
              //Case: Exponential, decaying to zero -> 
                //AR, use PACF to determine order
              //Case: Alternating +/-, decaying to zero ->
                //AR, use PACF to determin order
              //Case: One or more spikes, the rest at zero ->
                //MA, order determined wjere plot becomes 0
              //Case: Decay, starting after a few lags
                //Mixed AR/MA
              //Case: All zero or close to 0
                //Data is random
              //Case: No decay to zero
                //Data is not stationary


         //=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
         //STAGE 2: Model Identification
          //Find the order (p,q) for AR, MA
          // This is determined by applying the Akaike estimator to the data
          //Akaike Information Criteria (AIC)
          //AIC = 2k - 2ln(L); k=p+q+1; L=maximized value of the likelihood function
          //The values of p,q that minimize AIC should be taken as the correct order
            // Find AR order --> p
            // Find MA order --> q


         //=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //STAGE 3: Perform parameter estimation using maximum likelihood method
        /* (from Wikipedia)
         * Parameter estimation using computation algorithms to arrive at coefficients which 
         * best fit the selected ARIMA model. The most common methods use maximum likelihood 
         * estimation or non-linear least-squares estimation.
         */
        //Estimation of phi and theta 
         //Maximum Likelihood estimator
         //This is a non-linear optimisation problem, need to search for the max of the likelihood surface

         //Guess at initial values: Yule-Walker Estimation (pure autoregressive, i.e. q == 0) 
         		//(Wikipedia)Formulation as a least squares regression problem in which an ordinary least squares prediction 
         		//problem is constructed, basing prediction of values of Xt on the p previous values of the same 
         		//series. This can be thought of as a forward-prediction scheme. The normal equations for this 
         		//problem can be seen to correspond to an approximation of the matrix form of the Yule-Walker 
         		//equations in which each appearance of an autocovariance of the same lag is replaced by a slightly 
         		//different estimate.
         //or for q>0 use Hannan-Rissanan estimation
            

        /* STAGE 4: (from Wikipedia)
         * Model checking by testing whether the estimated model conforms to the specifications 
         * of a stationary univariate process. In particular, the residuals should be independent 
         * of each other and constant in mean and variance over time. (Plotting the mean and 
         * variance of residuals over time and performing a Ljung-Box test or plotting 
         * autocorrelation and partial autocorrelation of the residuals are helpful to identify 
         * misspecification.) If the estimation is inadequate, we have to return to step one 
         * and attempt to build a better model.
         */

         //calculate a vector of residuals (diff between model and data points)
         //generate white noise (Gaussian distributed) use Scalation random pkg to do this
         //if the residuals are random (i.e. if they have no autocorrelation) then the fit is good

         //return the vector of Model Parameters
         //result
    } // train

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the fit (??)
     */
    //def fit: // returning ??

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** For all the time points in vector t, predict the value of y = f(t) by ...
     *  @param z  the time-vector indicating time points to forecast
     */
    def predict (t: VectorD): Double = 
    {
      0.
      //return result of model function with fitted params
    } // predict

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Given several time vectors, forecast the y-values.
     *  @param z  the matrix containing row time-vectors to use for prediction
     */
    def predict (z: MatrixD): VectorD =
    {
        throw new UnsupportedOperationException ()
        //VectorD result
        //for(i<-0 until z.dim){
        //  result(i) = predict(z(i))
       // }
       //result
    } // predict

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Plot the autocorrelation function of a passed-in vector of data.
     *  @param t the data vector
     */
    def acf (t: VectorD): VectorD = 
    {
      val st   = new StatVector(t)
      val acfv = new VectorD(t.dim)
      for(i <- 0 until t.dim) acfv(i) = st.acorr(i)
      acfv
    } // acf

    def pacf (t: VectorD): VectorD = 
    {
      val acfv  = acf(t)
      val pacfv = new VectorD(t.dim - 1)
      for(i <- 0 until pacfv.dim) pacfv(i) = acfv(i + 1) - acfv(i)
      pacfv
    } // pacf

    def plotACF (acfv: VectorD)
    {
        val lag_axis = new VectorD(acfv.dim)
        val zero     = new VectorD(acfv.dim)
        for(i <- 0 until acfv.dim) lag_axis(i) = i
        new Plot (lag_axis, acfv, zero, "Plot of autocorrelation function")
    } // plotACF

    def plotPACF (pacfv: VectorD)
    {
        val lag_axis = new VectorD(pacfv.dim)
        val zero     = new VectorD(pacfv.dim)
        for(i <- 0 until pacfv.dim) lag_axis(i) = i
        new Plot (lag_axis, pacfv, zero, "Plot of partial autocorrelation function")
    } // plotACF


} // ARIMA class


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** This object is used to test the ARIMA class.
 */
object ARIMATest extends App
{
    val n = 100
    val y = new VectorD (n)
    val t = new VectorD (n)
    val r = Random ()
    for (i <- 0 until n) {
        t(i) = i.toDouble
        y(i) = t(i) + 10. * r.gen
    } // for

    val ts = new ARIMA (y, t)

    ts.train

    val z = ts.ma (5)
    //new Plot (t, y, z, "Plot of y, z vs. t")

    val v = ts.ar (new VectorD (.9, .7))
    //new Plot (t, y, v, "Plot of y, v vs. t")

} // ARIMATest object

//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** Object to test SimpleRegression class:  y = b dot x = (b0, b1) dot (1., x1).
 *  @see http://www.analyzemath.com/statistics/linear_regression.html
 */
object SimpleRegressionTest extends App
{
    // 5 data points: constant term, x1 coordinate
    val x = new MatrixD ((5, 2), 1., 0.,           // 5-by-2 matrix
                                 1., 1.,
                                 1., 2.,
                                 1., 3.,
                                 1., 4.)
    val y = new VectorD (2., 3., 5., 4., 6.)

    println ("x = " + x)
    println ("y = " + y)

    val rg = new SimpleRegression (x, y)
    rg.train ()
    println ("fit = " + rg.fit)

    val z  = new VectorD (1., 5.)           // predict y for one point
    val yp = rg.predict (z)
    println ("predict (" + z + ") = " + yp)

    val yyp = rg.predict (x)                // predict y for several points
    println ("predict (" + x + ") = " + yyp)

    new Plot (x.col(1), y, yyp)

} // SimpleRegressionTest object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** Object to test SimpleRegression class:  y = b dot x = b0 + b1*x1.
 *  @see http://mathbits.com/mathbits/tisection/Statistics2/linear.htm
 */
object SimpleRegressionTest2 extends App
{
    // 20 data points: just x1 coordinate
    val x1 = new VectorD (  4.,   9.,  10.,  14.,   4.,   7.,  12.,  22.,   1.,   3.,
                            8.,  11.,   5.,   6.,  10.,  11.,  16.,  13.,  13.,  10.)
    val y  = new VectorD (390., 580., 650., 730., 410., 530., 600., 790., 350., 400.,
                          590., 640., 450., 520., 690., 690., 770., 700., 730., 640.)

    println ("x1 = " + x1)
    println ("y  = " + y)

    val x  = MatrixD.form_cw (1., x1)       // form matrix x from vector x1
    val rg = new SimpleRegression (x, y)
    rg.train ()
    println ("fit = " + rg.fit)

    val z  = new VectorD (1., 15.)          // predict y for one point
    val yp = rg.predict (z)
    println ("predict (" + z + ") = " + yp)

    val yyp = rg.predict (x)                // predict y for several points
    println ("predict (" + x + ") = " + yyp)
    
    new Plot (x1, y, yyp)

} // SimpleRegressionTest2 object

