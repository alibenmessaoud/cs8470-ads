package ads.tsa

import scalation.analytics._
import scalation.linalgebra.{MatrixD, VectorD}
import scalation.plot.Plot
import scalation.random.Random
import scalation.util.Error

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** This class provide basic time series analysis capabilities for
 *  Auto-Regressive (AR), Integrated (I) and Moving Average (MA) models.
 *  In an ARIMA(p, d, q) model, p, d and q refer to the order of the
 *  Auto-Regressive, Integrated and Moving Average components of the model. 
 *  ARIMA models are often used for forecasting.
 *  @param y  the input vector
 *  @param t  the time vector
 */
class ARIMA (y: VectorD, t: VectorD)
      extends Predictor with Error
{
    private val n = y.dim        // size of the input vector

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
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

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
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
    def backwardDifference (f: VectorD, h: Int): VectorD = {
        val vec = new VectorD (f.dim - h) 
        for (i <- h until f.dim) {
            vec(i - h) = f(i) - f(i - h)
	} // for
        vec
    } // backwardDifference

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Fit an ARIMA model to historical times series data.
     */
    def train ()
    {

         val threshold = 10.0E-5
         var iter      = 0       
  
         var yt: VectorD         = null
         var r: SimpleRegression = null

         // Box-Jenkins using the Dickey–Fuller test
         // @see http://en.wikipedia.org/wiki/Box-Jenkins
         // @see http://en.wikipedia.org/wiki/Dickey%E2%80%93Fuller_test
         do {

             // increase iter
             iter += 1

             // get our backwards difference
             yt     = backwardDifference(y, iter)
             
             // pad our backwards difference for regression
             val padded = new VectorD(Array.fill(iter)(1.0)) ++ yt

             // build our time matrix
             val x      = new MatrixD (t.dim, 2)
             for (i <- 0 until t.dim) {
                 x(i, 0) = 1.0
                 x(i, 1) = t(i)
	     } // for

             // regression on x and padded yt
             r = new SimpleRegression(x, padded)
             r.train

         } while (r.fit._2 > threshold && iter < 3)

         

         println("iter = %d".format(iter))

         

         // Find AR order --> p
              //for p = 1, acf(yt) "should have an exponentially decreasing appearance"
              //for p > 1, pacf(yt) should become 0 at lag p+1

         // Find MA order --> q
              // let i = the lag where acf(yt) becomes 0
              // then q = i-1

        /* Stage 2: (from Wikipedia)
         * Parameter estimation using computation algorithms to arrive at coefficients which 
         * best fit the selected ARIMA model. The most common methods use maximum likelihood 
         * estimation or non-linear least-squares estimation.
         */

         //This is a non-linear optimisation problem, need to search for the max of the likelihood surface

         //Guess at initial values: Yule-Walker Estimation (pure autoregressive) 
         //or for q>0 use Hannan-Rissanan estimation


        /* Stage 3: (from Wikipedia)
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
         //TODO: we may not even have to generate white noise here??


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
        0.      // FIX: to be implemented
    } // predict

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Given several time vectors, forecast the y-values.
     *  @param z  the matrix containing row time-vectors to use for prediction
     */
    def predict (z: MatrixD): VectorD =
    {
        throw new UnsupportedOperationException ()
    } // predict

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
    new Plot (t, y, z, "Plot of y, z vs. t")

    val v = ts.ar (new VectorD (.9, .7))
    new Plot (t, y, v, "Plot of y, v vs. t")

} // ARIMATest object

