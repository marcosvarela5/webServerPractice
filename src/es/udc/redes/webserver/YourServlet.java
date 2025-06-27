package es.udc.redes.webserver;
import java.util.Map;

/**
 * This class must be filled to complete servlets option (.do requests).
 */
public class YourServlet implements MiniServlet {
	

	public YourServlet(){
		
	}

        @Override
	public String doGet (Map<String, String> parameters){
			String firstNumber = parameters.get("firstNumber");
			String secondNumber = parameters.get("secondNumber");
			int sum = (Integer.parseInt(firstNumber)+Integer.parseInt(secondNumber));
			String result =  sum+"";
		return printHeader() + printBody(result) + printEnd();
	}	

	private String printHeader() {
		return "<html><head> <title>Greetings</title> </head>";
	}


	private String printBody(String result) {
		return "<body> <h1> Result is " + result + "</h1></body>";
	}

	private String printEnd() {
		return "</html>";
	}
}

