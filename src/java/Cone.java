import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/Cone"})
public class Cone extends HttpServlet {

    /**
     * Lista obiektow klasy Map do przechowywanie danych o defektach
     */
    private static final List<Map<String, Double>> LIST_OF_DEFECTS = new ArrayList<>();

    
    /**
     * Oblicza mase calkowita elipsoidu o podanych parametrach.
     *
     * @author Michal Stys
     * @param ra
     * @param rb
     * @param h
     * @param c Dane o polosi c elipsoidy
     * @param g gęstosc wlasciwa kul
     * @return Masa calkowita bryly z defektami
     */
    public double monteCarlo(double ra, double rb, double h, double g, double c) {

        Random generator = new Random();
        double total_masa;

        int k_cone = 0;         // trafione proby 
        int k_kule = 0;              // trafion proby kul
        int n = 100000;            // ilosc prob

        double V_cuboid;       //obj prostopadłościanu opisanego
        double V_Cone;
        double V_shapes;

        //wymiary opisanego prostopadłościanu na elipsoidzie
        double x_min = ra*-1;  
        double x_max = ra;
        double y_min = 0;
        double y_max = h;
        double z_min = ra*-1;
        double z_max = ra;
        
        double tan_a = h/(ra-rb);

        V_cuboid = 2 * ra * 2 * ra *  h;

        for (int i = 0; i < n; i++) {

            // losowe punkty z zdanego przedzialu
            double x_rand = generator.nextDouble() * (x_max - x_min) + x_min;
            double y_rand = generator.nextDouble() * (y_max - y_min) + y_min;
            double z_rand = generator.nextDouble() * (z_max - z_min) + z_min;

            
            double y_len = ((y_rand/tan_a)-ra)*-1;
            double segment = Math.sqrt((x_rand*x_rand)+(z_rand*z_rand)) ;
            if (segment <= y_len) {
                k_cone++;
            }

            // sprawdzenie czy wylosowany punkt znajduje się w jednej z kul
            int listSize = LIST_OF_DEFECTS.size();
            for (int j = 0; j < listSize; j++) {

                Map<String, Double> defect;
                defect = LIST_OF_DEFECTS.get(j);

                double x = defect.get("x"); // współrzędna x kuli
                double y = defect.get("y"); // współrzędna y kuli
                double z = defect.get("z"); // współrzędna z kuli
                double r = defect.get("r"); // r kuli

                //sprawdzenie czy losowy punkt zawarty w prostopadłoscianie zawiera się w kuli
                double segment_defect = Math.sqrt(pow((x_rand - x), 2) + pow((y_rand - y), 2) + pow((z_rand - z), 2));
                if (segment_defect <= r) {
                    k_kule++;
                }
            }
        }

        V_Cone = (double) (k_cone - k_kule) / n * V_cuboid;    // obliczenie objetosci 
        V_shapes = (double) k_kule / n * V_cuboid;                      // obliczenie objetosci kul

        total_masa = V_Cone * c + V_shapes * g;                     // Masa calkowita, v - gęstość właściwa elipsoidu, g - gęstość właściwa kul
        total_masa = (double) Math.round(total_masa * 100) / 100;       // zaokraglenie wyniku do 10^-2
        
        return total_masa;
    }
    
    /**
     * 
     * @param param String paramametr do przygotowania
     * @return double 
     */
    double prepareParam(String param) {
        return Double.parseDouble(param.replace(',', '.'));
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ellipsoid</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ellipsoid at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
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
            throws ServletException, IOException {

        if ( request.getParameter("x") != null &&
             request.getParameter("y") != null && 
             request.getParameter("z") != null &&
             request.getParameter("r") != null ) {

            Map<String, Double> parameters = new HashMap<>();

            parameters.put("x", prepareParam(request.getParameter("x")));
            parameters.put("y", prepareParam(request.getParameter("y")));
            parameters.put("z", prepareParam(request.getParameter("z")));
            parameters.put("r", prepareParam(request.getParameter("r")));

            LIST_OF_DEFECTS.add(parameters);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            
        } else {

            response.setContentType("text/html");
            PrintWriter pw = response.getWriter();
            pw.print("Not enought parameters");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

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
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        if ( request.getParameter("ra") != null &&
             request.getParameter("rb") != null &&
             request.getParameter("h") != null &&
             request.getParameter("c") != null &&
             request.getParameter("g") != null ) {

            double ra = prepareParam(request.getParameter("ra"));
            double rb = prepareParam(request.getParameter("rb"));
            double h = prepareParam(request.getParameter("h"));
            double c = prepareParam(request.getParameter("c"));
            double g = prepareParam(request.getParameter("g"));

            double total_masa = monteCarlo(ra, rb, h, g, c);

            pw.print(total_masa);
            response.setStatus(HttpServletResponse.SC_OK);

        } else {

            pw.print("Not enought parameters");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
