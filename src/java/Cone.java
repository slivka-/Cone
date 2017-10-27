import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Michał Śliwa
 */
@WebServlet(urlPatterns =
{
    "/Cone"
})
public class Cone extends HttpServlet
{
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            if (request.getMethod().equals("GET"))
            {
                try
                {
                    double x = Double.parseDouble(request.getParameter("x"));
                    double y = Double.parseDouble(request.getParameter("y"));
                    double z = Double.parseDouble(request.getParameter("z"));
                    double r = Double.parseDouble(request.getParameter("r"));
                    ConeModel.getInstance().addDefect(x, y, z, r);
                }
                catch(NumberFormatException ex)
                {
                    out.print(ex);
                }
            }
            else if (request.getMethod().equals("POST"))
            {
                try
                {
                    double ra = Double.parseDouble(request.getParameter("ra"));
                    double rb = Double.parseDouble(request.getParameter("rb"));
                    double h = Double.parseDouble(request.getParameter("h"));
                    double c = Double.parseDouble(request.getParameter("c"));
                    double g = Double.parseDouble(request.getParameter("g"));
                    ConeModel.getInstance().setConeMeas(ra, rb, h, c, g);
                    double mass = ConeModel.getInstance().calculateMass();
                    String massString = String.format("%f", mass);
                    out.write(massString);
                    ConeModel.destroyInstance();
                }
                catch(NumberFormatException ex)
                {
                    out.print(ex);
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>
    
    
}

/**
 * Represensts a cone.
 * Point 0,0,0 is located in the middle of the base
 * 
 * @author Michał Śliwa
 */
class ConeModel
{
    // <editor-fold defaultstate="collapsed" desc="Instancing methods">
    private static ConeModel instance;
    
    private ConeModel() {} //private constructor to prevent instantiation
    
    public static ConeModel getInstance()
    {
        if(instance == null)
            instance = new ConeModel();
        return instance;
    }
    
    public static void destroyInstance()
    {
        instance = null;
    }
    // </editor-fold>
    
    private final int CONE_N = 10000;
    private final int DEFECT_N = 1000;
    private final int SPHERE_N = 1000;
    
    private double RA; //base radius [m]
    private double RB; //top radius [m]
    private double H; //height [m]
    private double C; //cone material density [kg/m^3]
    private double G; //defects density [kg/m^3]
    private double TANA; //tangent of cone base angle
    private final ArrayList<Defect> defects = new ArrayList<>();
    
    /**
     * Calculates mass of the cone
     * 
     * @return cones mass in kg
     */
    public double calculateMass()
    {
        //calculates volmue of the cone using monte carlo method
        double cuboidVol = (2*RA)*(2*RA)*H;
        int coneK = 0;
        for(int i=0;i<CONE_N;i++)
        {
            double x = ThreadLocalRandom.current().nextDouble(RA*-1, RA);
            double z = ThreadLocalRandom.current().nextDouble(RA*-1, RA);
            double y = ThreadLocalRandom.current().nextDouble(0, H);
            if(isInCone(x, y, z))
                coneK++;
        }
        double coneVol = cuboidVol*((double)coneK/(double)CONE_N);
        
        //calculates volume of defects in cone using monte carlo method
        double defectsVol = 0.0;
        int defectK;
        for(int j = 0;j<defects.size();j++)
        {
            Defect d = defects.get(j); //get current defect
            defectK = 0;
            for(int i=0;i<DEFECT_N;i++)
            {
                //get random point inside defect
                Point3D rPoint = d.getRandomPoint(); 
                //check if point is in cone
                if(isInCone(rPoint.getX(), rPoint.getX(),rPoint.getZ()))
                {
                    Boolean isIn = false;
                    //check if point is in any previous defects
                    for (int k=0;k<j;k++)
                    {
                        if(defects.get(k).isInSphereRelative(rPoint))
                        {
                            isIn = true;
                            break;
                        }
                    }
                    if(!isIn)
                        defectK++;
                }
            }
            //calculate defect volume and add to previous value
            defectsVol += d.getVolume()*((double)defectK/(double)DEFECT_N);
        }
        //calculate cone mass
        double coneMass = ((coneVol-defectsVol)*C)+(defectsVol*G);
        return coneMass;
    }
    
    /**
    * Checks if point is inside cone 
    * 
    * @param x x coord
    * @param y y coord
    * @param z z coord
    * @return true if point is inside cone, otherwise false
    */
    private Boolean isInCone(double x, double y, double z)
    {
        double radiusOnY = -1*((y/TANA)-RA);
        double pointDist = Math.sqrt((x*x)+(z*z));
        return pointDist <= radiusOnY;
    }
        
    /**
     * Sets cone measurements, density and defects density
     * 
     * @param ra base radius
     * @param rb top radius
     * @param h height
     * @param c cone material density
     * @param q defects density
     */
    public void setConeMeas(double ra, double rb, double h, double c, double g)
    {
        this.RA = ra;
        this.RB = rb;
        this.H = h;
        this.C = c;
        this.G = g;
        this.TANA = H/(RA-RB);
    }
    
    /**
     * Adds a defect to cone at given coordinates, with given radius
     * 
     * @param x x coord
     * @param y y coord
     * @param z z coord
     * @param r radius
     */
    public void addDefect(double x, double y, double z, double r)
    {
        defects.add(new Defect(x, y, z, r));
    }
    
    /**
     * Represents a spherical defect inside of the cone.
     */
    class Defect
    {
        private final double X; //x coord of defect
        private final double Y; //y coord of defect
        private final double Z; //z coord of defect
        private final double R; //radius of defect [m]
        
        public Defect(double x, double y, double z, double r)
        {
            this.X = x;
            this.Y = y;
            this.Z = z;
            this.R = r;
        }
       
        /**
         * Returns point inside the sphere relative to its coordinates
         * 
         * @return point in 3D space
         */
        public Point3D getRandomPoint()
        {
            Point3D output;
            do{
                
                double x = X + ThreadLocalRandom.current().nextDouble(R*-1, R);
                double y = Y + ThreadLocalRandom.current().nextDouble(R*-1, R);
                double z = Z + ThreadLocalRandom.current().nextDouble(R*-1, R);
                output = new Point3D(x, y, z);
            }while(!isInSphereRelative(output));
            return output;
        }
        
        /**
         * Calculates volume of the sphere
         * 
         * @return volume of the sphere
         */
        public double getVolume()
        {
            double cubeVolume = Math.pow(2*R,3);
            int k = 0;
            for(int i=0;i<SPHERE_N;i++)
            {
                double x = ThreadLocalRandom.current().nextDouble(R*-1, R);
                double z = ThreadLocalRandom.current().nextDouble(R*-1, R);
                double y = ThreadLocalRandom.current().nextDouble(R*-1, R);
                if(isInSphere(x, y, z))
                    k++;
            }
            double sphereVol = cubeVolume*((double)k/(double)SPHERE_N);
            return sphereVol;
        }
        
        /**
         * Checks if point is inside sphere 
         * 
         * @param x x coord
         * @param y y coord
         * @param z z coord
         * @return true if point is inside sphere, otherwise false
         */
        private Boolean isInSphere(double x, double y, double z)
        {
            double dist = Math.sqrt(Math.pow(x,2) 
                                  + Math.pow(y,2) 
                                  + Math.pow(z,2));
            return dist<=R;
        }
        
        /**
         * Checks if point is inside sphere relative to its coorddinates
         * 
         * @param p point in space
         * @return true if point is inside sphere, otherwise false
         */
        public Boolean isInSphereRelative(Point3D p)
        {
            double dist = Math.sqrt(Math.pow(X-p.getX(),2) 
                                  + Math.pow(Y-p.getY(),2) 
                                  + Math.pow(Z-p.getZ(),2));
            return dist<=R;
        }
    }
    
    /**
     * Represents point in 3D space
     */
    class Point3D
    {
        private final double x;
        private final double y;
        private final double z;
        
        public Point3D(double _x, double _y, double _z)
        {
            this.x = _x;
            this.y = _y;
            this.z = _z;
        }
       
        public double getX()
        {
            return x;
        }

        public double getY()
        {
            return y;
        }

        public double getZ()
        {
            return z;
        }
    }
}

