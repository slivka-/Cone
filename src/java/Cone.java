import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            if (request.getMethod().equals("GET"))
            {
                try
                {
                    int x = Integer.parseInt(request.getParameter("x"));
                    int y = Integer.parseInt(request.getParameter("y"));
                    int z = Integer.parseInt(request.getParameter("z"));
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
    // </editor-fold>
    
    private double RA; //base radius [m]
    private double RB; //top radius [m]
    private double H; //height [m]
    private double C; //cone material density [kg/m^3]
    private double G; //defects density [kg/m^3]
    private final ArrayList<Defect> defects = new ArrayList<>();
    
    /**
     * Calculates mass of the cone
     * 
     * @return cones mass in kg
     */
    public double calculateMass()
    {
        return 0.0;
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
    }
    
    /**
     * Adds a defect to cone at given coordinates, with given radius
     * 
     * @param x x coord
     * @param y y coord
     * @param z z coord
     * @param r radius
     */
    public void addDefect(int x, int y, int z, double r)
    {
        defects.add(new Defect(x, y, z, r));
    }
    
    /**
     * Represents a spherical defect inside of the cone.
     */
    class Defect
    {
        private final int X;//x coord of defect
        private final int Y;//y coord of defect
        private final int Z;//z coord of defect
        private final double R;//radius of defect [m]
        
        public Defect(int x, int y, int z, double r)
        {
            this.X = x;
            this.Y = y;
            this.Z = z;
            this.R = r;
        }

        public int getX() { return X; }

        public int getY() { return Y; }

        public int getZ() { return Z; }

        public double getR() { return R; }
    }
}
