package org.utdallas.atos.training.recommendationengine;

import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2Context;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookResponse;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfStamper;

import org.utdallas.atos.training.recommendationengine.model.*;
import org.utdallas.atos.training.recommendationengine.repository.*;
import org.utdallas.atos.training.recommendationengine.service.EmployeeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import java.net.URL;
import java.net.MalformedURLException;

@RestController
public class MainController
{
    //TODO IMPORTANT!!!, place email that you want to send the pdf to into this demoEmployee, it is the 3rd value, and that it matches the recpient variable right below it
    Employees demoEmployee = new Employees("John", "Bob", "atosseniordesign@gmail.com", "972123456", "worker", 10, "suzy", "password20");

	//GMAIL ACCOUNT OF THE SENDING BOT, you should make your own though
    //user: atosdemo525
    //pass: demo1234+

    private static String USER_NAME = "atosdemo525";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "demo1234+"; // GMail password
    private static String RECIPIENT = "atosseniordesign@gmail.com"; //email of reciever


	JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

	@Autowired
	private CoursesRepository courses;
	@Autowired
	private EmployeesRepository employees;
	@Autowired
	private JobsRepository jobs;
	@Autowired
	private ReferencesRepository references;
	@Autowired
	private SkillsRepository skills;
	@Autowired
	private ToolsRepository tools;

    @Autowired
	EmployeeServiceImpl employeeService;

  	@GetMapping("/")
  	public String hello() {
    	return "Seems like this is our last run captain. I'll give the calculations my all.";
  	}

	@PostMapping("/webhook")
	public GoogleCloudDialogflowV2WebhookResponse webhook(@RequestBody String rawString) throws IOException {

		//store json into dialogflow request object
		GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(rawString).parse(GoogleCloudDialogflowV2WebhookRequest.class);

		//grab intent of json
		String intent = request.getQueryResult().getIntent().getDisplayName();

		//create dialogflow response object
		GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
		
		
		
       // if (intent.matches("Default Welcome Intent")){
		//	checkID(request, response);
		//}
		//else{
			//employeeService.saveEmployee(demoEmployee);
			//run logic according to passed intent
			matchIntent(request, response, intent);
		//}
		
		//test stuff - comment out when doing the id check instead
		//employeeService.saveEmployee(demoEmployee);
		//matchIntent(request, response, intent);

        //send response back to dialogflow
        return response;
	}

	//matches intent to its intended function
	private void matchIntent(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String intent)
    {
	/*
        AddGoal conversation intents
        if(intent.matches("AddGoal_Awaiting_Goal_Correct|AddGoal_Awaiting_Goal_Correct_1|AddGoal_Initialize_Goal_Named"))
            returnSubdomains(request, response, "Skill");
        else if(intent.matches("AddGoal_Awaiting_Goal_Skill_Subdomain_Correct|AddGoal_Awaiting_Goal_Skill_Subdomain_Correct_1"))
            returnListOfItems(request, response, "Skill");
        else if(intent.matches("AddGoal_Skill_List_Loop_Yes|AddGoal_Skill_List_Loop_Yes_1"))
            loopItemList(request, response, "Skill", "Yes");
        else if(intent.matches("AddGoal_Skill_List_Loop_No|AddGoal_Skill_List_Loop_No_1"))
            loopItemList(request, response, "Skill", "No");
        else if(intent.matches("AddGoal_Skill_list_Loop_Info|AddGoal_Skill_list_Loop_Final_Info|AddGoal_Skill_List_Loop_Info_1|AddGoal_Skill_List_Loop_Final_Info_1"))
            getItemInfo(request, response, "Skill");
        else if(intent.matches("AddGoal_Awaiting_Goal_Skill_Subdomain_All|AddGoal_Awaiting_Goal_Skill_Subdomain_All_1"))
            loopAllSubdomains(request, response, "Skill");
        else if(intent.matches("AddGoal_Awaiting_Goal_Skill_Subdomain_No|AddGoal_Awaiting_Goal_Skill_Subdomain_No_1"))
            goToTools(request, response, "Tool");
        else if(intent.matches("AddGoal_Skill_List_Loop_Final_Yes|AddGoal_Skill_List_Loop_Final_Yes_1"))
            finalItemList(request, response, "Skill", "Yes");
        else if(intent.matches("AddGoal_Skill_List_Loop_Final_No|AddGoal_Skill_List_Loop_Final_No_1"))
            finalItemList(request, response, "Skill", "No");
        else if(intent.matches("AddGoal_Awaiting_Goal_Tool_Subdomain_Correct|AddGoal_Awaiting_Goal_Tool_Subdomain_Correct_1"))
            returnListOfItems(request, response, "Tool");
        else if(intent.matches("AddGoal_Awaiting_Goal_Tool_Subdomain_All|AddGoal_Awaiting_Goal_Tool_Subdomain_All_1"))
            loopAllSubdomains(request, response, "Tool");
        else if(intent.matches("AddGoal_Tool_List_Loop_Yes|AddGoal_Tool_List_Loop_Yes_1"))
            loopItemList(request, response, "Tool", "Yes");
        else if(intent.matches("AddGoal_Tool_List_Loop_No|AddGoal_Tool_List_Loop_No_1"))
            loopItemList(request, response, "Tool", "No");
        else if(intent.matches("AddGoal_Tool_list_Loop_Info|AddGoal_Tool_list_Loop_Final_Info|AddGoal_Tool_List_Loop_Info_1|AddGoal_Tool_List_Loop_Final_Info_1"))
            getItemInfo(request, response, "Tool");
        else if(intent.matches("AddGoal_Tool_List_Loop_Final_Yes|AddGoal_Tool_List_Loop_Final_Yes_1"))
            finalItemList(request, response, "Tool", "Yes");
        else if(intent.matches("AddGoal_Tool_List_Loop_Final_No|AddGoal_Tool_List_Loop_Final_No_1|"))
            finalItemList(request, response, "Tool", "No");
        else if(intent.matches("AddGoal_Awaiting_Goal_Tool_Subdomain_No"))
            finalItemList(request, response, "Tool", "No");
	*/
		//add goal conversation intents, changed to switch statement in order to make adding/removing/changing intents easier
		String requestSwitchCompare;
		switch(intent) {
			case "checkID":
				checkID(request, response);
				break;
			case "AddGoal_Awaiting_Goal_Correct":					returnSubdomains(request, response, "Skill");
																	break;
			case "AddGoal_Awaiting_Goal_Correct_1":					returnSubdomains(request, response, "Skill");
																	break;
			case "AddGoal_Initialize_Goal_Named":					returnSubdomains(request, response, "Skill");
																	break;
			case "AddGoal_Awaiting_Goal_Skill_Subdomain_Correct":	returnListOfItems(request, response, "Skill");
																	break;
			case "AddGoal_Awaiting_Goal_Skill_Subdomain_Correct_1":	returnListOfItems(request, response, "Skill");
																	break;

			case "AddGoal_Skill_List_Loop_Yes":						loopItemList(request, response, "Skill", "Yes");
																	break;
			case "AddGoal_Skill_List_Loop_Yes_1":					loopItemList(request, response, "Skill", "Yes");
																	break;

			case "AddGoal_Skill_List_Loop_No":						loopItemList(request, response, "Skill", "No");
																	break;
			case "AddGoal_Skill_List_Loop_No_1":					loopItemList(request, response, "Skill", "No");
																	break;

			case "AddGoal_Skill_list_Loop_Info":					getItemInfo(request, response, "Skill");
																	break;
			case "AddGoal_Skill_list_Loop_Final_Info":				getItemInfo(request, response, "Skill");
																	break;
			case "AddGoal_Skill_List_Loop_Info_1":					getItemInfo(request, response, "Skill");
																	break;
			case "AddGoal_Skill_List_Loop_Final_Info_1":			getItemInfo(request, response, "Skill");
																	break;

			case "AddGoal_Awaiting_Goal_Skill_Subdomain_All":		loopAllSubdomains(request, response, "Skill");
																	break;
			case "AddGoal_Awaiting_Goal_Skill_Subdomain_All_1":		loopAllSubdomains(request, response, "Skill");
																	break;

			case "AddGoal_Awaiting_Goal_Skill_Subdomain_No":		goToTools(request, response, "Tool");
																	break;
			case "AddGoal_Awaiting_Goal_Skill_Subdomain_No_1":		goToTools(request, response, "Tool");
																	break;

			case "AddGoal_Skill_List_Loop_Final_Yes":				finalItemList(request, response, "Skill", "Yes");
																	break;
			case "AddGoal_Skill_List_Loop_Final_Yes_1":				finalItemList(request, response, "Skill", "Yes");
																	break;

			case "AddGoal_Skill_List_Loop_Final_No":				finalItemList(request, response, "Skill", "No");
																	break;
			case "AddGoal_Skill_List_Loop_Final_No_1":				finalItemList(request, response, "Skill", "No");
																	break;

			case "AddGoal_Awaiting_Goal_Tool_Subdomain_Correct":	returnListOfItems(request, response, "Tool");
																	break;
			case "AddGoal_Awaiting_Goal_Tool_Subdomain_Correct_1":	returnListOfItems(request, response, "Tool");
																	break;

			case "AddGoal_Awaiting_Goal_Tool_Subdomain_All":		loopAllSubdomains(request, response, "Tool");
																	break;
			case "AddGoal_Awaiting_Goal_Tool_Subdomain_All_1":		loopAllSubdomains(request, response, "Tool");
																	break;

			case "AddGoal_Tool_List_Loop_Yes":						loopItemList(request, response, "Tool", "Yes");
																	break;
			case "AddGoal_Tool_List_Loop_Yes_1":					loopItemList(request, response, "Tool", "Yes");
																	break;

			case "AddGoal_Tool_List_Loop_No":						loopItemList(request, response, "Tool", "No");
																	break;
			case "AddGoal_Tool_List_Loop_No_1":						loopItemList(request, response, "Tool", "No");
																	break;

			case "AddGoal_Tool_list_Loop_Info":						getItemInfo(request, response, "Tool");
																	break;
			case "AddGoal_Tool_list_Loop_Final_Info":				getItemInfo(request, response, "Tool");
																	break;
			case "AddGoal_Tool_List_Loop_Info_1":					getItemInfo(request, response, "Tool");
																	break;
			case "AddGoal_Tool_List_Loop_Final_Info_1":				getItemInfo(request, response, "Tool");
																	break;

			case "AddGoal_Tool_List_Loop_Final_Yes":				finalItemList(request, response, "Tool", "Yes");
																	break;
			case "AddGoal_Tool_List_Loop_Final_Yes_1":				finalItemList(request, response, "Tool", "Yes");
																	break;

			case "AddGoal_Tool_List_Loop_Final_No":					finalItemList(request, response, "Tool", "No");
																	break;
			case "AddGoal_Tool_List_Loop_Final_No_1":				finalItemList(request, response, "Tool", "No");
																	break;
			case "AddGoal_Awaiting_Goal_Tool_Subdomain_No":			finalItemList(request, response, "Tool", "No");
																	break;
			//new intents will go here
			default:												response.setFulfillmentText("Failed to catch intent");
																	break;
		}


    }
    //next three functions are helper function i made so that it is easier to mess with contexts
    //extracts specified context parameter from the sent dialogflow payload
	private List<String> extractParameter(GoogleCloudDialogflowV2WebhookRequest request, String nameOfContext, String nameOfParameter){
        GoogleCloudDialogflowV2Context foundContext = findContext(request, nameOfContext);
        String parameterList = foundContext.getParameters().get(nameOfParameter).toString();

        parameterList = parameterList.replace("[", "").replace("]", "");
        List<String> listOfParameters = new ArrayList<String>(Arrays.asList(parameterList.split(", ")));

        return listOfParameters;
    }

    //adds a parameter to the specified context
    private void addParameter(GoogleCloudDialogflowV2WebhookRequest request, String nameOfContext, String nameOfParameter, Object objectToBeAdded){

        GoogleCloudDialogflowV2Context foundContext = findContext(request, nameOfContext);

        Map<String,Object> param = new HashMap<String, Object>();
        param = foundContext.getParameters();
        param.put(nameOfParameter, objectToBeAdded);

        foundContext.setParameters(param);
    }

    //looks for a context, and returns the context object if found
    private GoogleCloudDialogflowV2Context findContext(GoogleCloudDialogflowV2WebhookRequest request, String nameOfContext){
        Iterator<GoogleCloudDialogflowV2Context> iter = request.getQueryResult().getOutputContexts().iterator();
        GoogleCloudDialogflowV2Context iterContext = new GoogleCloudDialogflowV2Context();

        String name;
        boolean flag = true;

        //while there is another value to iterate to and that value's name is not the string passed
        while(iter.hasNext() && flag){
            iterContext = iter.next();
            name = iterContext.getName();

            if(name.equals(nameOfContext))
                flag = false;
        }

        return iterContext;
    }

    boolean containsItem(List<?> list, List<?> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void returnSubdomains(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type){
	    List<String> jobName = extractParameter(request, request.getSession() + "/contexts/session_variable", "job_type");

        List<Jobs> jobResult = jobs.findAllByNameContains(jobName.get(0));

        //obtain a set of of unique sub-domain for skills or tools
        HashSet<String> uniqueSubdomains = new HashSet<String>();

        //create a response string of all found subdomains
        StringBuilder responseString = new StringBuilder();

        //if looking for skill subdomain else get tool subdomains
        if(type.equals("Skill")){
            responseString.append("Sounds like a great choice. To help you on your way, here are some fields that are related to the career path you specified. Are you familiar with any of them?<br>");
            List<Skills> skillList = skills.findByJobs(jobResult.get(0));
            for (Skills skillObject: skillList) {
                uniqueSubdomains.add(skillObject.getSubDomain());
            }
        }
        else if(type.equals("Tool")){
            //HOW TO REACT AFTER USER COMES HERE BY  HAVING ALREADY MASTERED SKILLS
            List<String> extractedSkill = extractParameter(request, request.getSession() + "/contexts/session_variable", "current_skill");

            //if no skills were needed
            if(extractedSkill.get(0).equals("-1"))
            {
                responseString.append("Great! That's all I need to ask about skills I could find for those fields. Now I'm going to ask questions about your knowledge of related tool subdomains. Are you familiar with any of these?<br>");
            }
            else
            {
                responseString.append("Understood, now I'm going to ask questions about your knowledge of related tool subdomains. Are you familiar with any of these?<br>");
            }

            List<Tools> toolList = tools.findByJobs(jobResult.get(0));
			//jump from here to send the email stuff
			/*if (toolList.size() == 0){
				sendPdf(request);
				response.setFulfillmentText(responseString.toString()+ "Thank you for the chat! I'll send you an email for a recommendation plan on the courses that will best help you advance in your career path.<br>");
			}*/
            for (Tools toolObject : toolList) {
                uniqueSubdomains.add(toolObject.getSubDomain());
            }
        }

        //TODO
        addParameter(request,request.getSession() + "/contexts/session_variable", "uniqueSubdomainList", uniqueSubdomains );
        response.setOutputContexts(request.getQueryResult().getOutputContexts());

        //append subdomains found to response string
        for(String uniqueSubDomain : uniqueSubdomains){
                responseString.append(uniqueSubDomain + "<br>");
        }
        response.setFulfillmentText(responseString.toString());
    }

    private void returnListOfItems(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type){

	    //context set based off if tools or skills
	    String finalLoop = "";
	    String loop = "";
	    String parameterName = "";
        String retryAnswer = "";

        //store user inputted subdomain
        List<String> subdomainList = new ArrayList<>();

        //name of job passed from previous context
        List<String> jobName = extractParameter(request, request.getSession() + "/contexts/session_variable", "job_type");

        //grab job by job name
        List<Jobs> jobFound = jobs.findAllByNameContains(jobName.get(0));

        //will contain list of tools or skills
        List<String> listOfItems= new ArrayList<>();

        String initialPrompt = "";
        if(type.equals("Skill")){
            initialPrompt = "I'm going to ask questions on specific skill familiarity based on your response, you can ask for more info on skills if needed. Are you familiar with any of the following?<br>";
            finalLoop = "/contexts/skill_list_loop_final";
            loop = "/contexts/skill_list_loop";
            parameterName = "current_skill";
            subdomainList = extractParameter(request, request.getSession() + "/contexts/session_variable", "skill_subdomain_type");

            //List of skills based on job id and subdomains passed
            List<Skills> skillList = skills.findByJobs_IdAndSubDomainIn(jobFound.get(0).getId(), subdomainList);

            skillList = employeeService.removeLearnedSkills(skillList, demoEmployee);
            //create a list of named skills
            for (Skills toolObject: skillList) {
                listOfItems.add(toolObject.getName());
            }
        }
        else if(type.equals("Tool")){
            initialPrompt = "I'm going to ask about your knowledge of specific tools based on your response, you can ask for more info if needed. Are you familiar with any of these?<br>";
            finalLoop = "/contexts/tool_list_loop_final";
            loop = "/contexts/tool_list_loop";
            parameterName = "current_tool";
            subdomainList = extractParameter(request, request.getSession() + "/contexts/session_variable", "tool_subdomain_type");

            //List of tools based on job id and subdomains passed
            List<Tools> toolList = tools.findByJobs_IdAndSubDomainIn(jobFound.get(0).getId(), subdomainList);

            toolList = employeeService.removeLearnedTools(toolList, demoEmployee);

            //create a list of named tools
            for (Tools toolObject: toolList) {
                listOfItems.add(toolObject.getName());
            }
        }

        //grab current contexts passed by dialogflow
        List<GoogleCloudDialogflowV2Context> contextList = request.getQueryResult().getOutputContexts();

        //create new context with life span of 1
        GoogleCloudDialogflowV2Context loopFinalContext = new GoogleCloudDialogflowV2Context();


        List<String> legalSubdomain = extractParameter(request, request.getSession() + "/contexts/session_variable", "uniqueSubdomainList");

        //grab last element in list of skills or tools
        String lastElementPrompt = new String("default skill prompt");

        //if size of items is 0 before removal then the user already learned all possible skills
        if(listOfItems.size() == 0 && type.equals("Skill"))
        {
            addParameter(request, request.getSession() + "/contexts/session_variable", parameterName, "-1");
            returnSubdomains(request, response, "Tool");
            loopFinalContext.setName(request.getSession() + "/contexts/awaiting_goal_tool_subdomain");
            loopFinalContext.setLifespanCount(1);
            contextList.add(loopFinalContext);
            response.setOutputContexts(contextList);
            return;
        }
        else if(listOfItems.size() == 0 && type.equals("Tool"))
        {
            addParameter(request, request.getSession() + "/contexts/session_variable", parameterName, "-1");
            finalItemList(request, response, "Tool", "No");
            return;
        }

        lastElementPrompt = listOfItems.get(listOfItems.size() - 1);
        listOfItems.remove(listOfItems.size() - 1);

        //if there are no more items in the list after first removal then change context to final loop, else set to loop
        if(listOfItems.size() == 0){
            addParameter(request, request.getSession() + "/contexts/session_variable", parameterName, lastElementPrompt);
            loopFinalContext.setName(request.getSession() + finalLoop);

        } else if(listOfItems.size() > 0){
            addParameter(request, request.getSession() + "/contexts/session_variable", "item_list_loop", listOfItems);
            addParameter(request, request.getSession() + "/contexts/session_variable", parameterName, lastElementPrompt);
            loopFinalContext.setName(request.getSession() + loop);
        }
        loopFinalContext.setLifespanCount(1);
        contextList.add(loopFinalContext);
        response.setOutputContexts(contextList);
        response.setFulfillmentText(initialPrompt + lastElementPrompt);
    }

    private void loopItemList(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type, String userResponse){
        String parameterName = "";
        String finalLoop = "";
        String loop = "";
        List<String> itemList = extractParameter(request, request.getSession() + "/contexts/session_variable", "item_list_loop");

        //update context information based on skills or tools looping
        if(type.equals("Skill")){
            parameterName = "current_skill";
            finalLoop = "/contexts/skill_list_loop_final";
            loop = "/contexts/skill_list_loop";

            if(userResponse == "Yes")
            {
                List<String> currentSkill = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
                Skills foundSkill = skills.findByName(currentSkill.get(0));
                employeeService.addSkillToEmployee(foundSkill, demoEmployee);
            }
        }
        else{
            parameterName = "current_tool";
            finalLoop = "/contexts/tool_list_loop_final";
            loop = "/contexts/tool_list_loop";

            if(userResponse == "Yes")
            {
                List<String> currentTool = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
                List<Tools> foundTools = tools.findByName(currentTool.get(0));
                employeeService.addToolToEmployee(foundTools.get(0), demoEmployee);

            }
        }



        //grab last item in list
        int listSize = itemList.size();
        String lastElementPrompt = itemList.get(listSize - 1);
        itemList.remove(listSize - 1);


        //update item list left to iterate through
        addParameter(request, request.getSession() + "/contexts/session_variable", "item_list_loop", itemList);
        addParameter(request, request.getSession() + "/contexts/session_variable", parameterName, lastElementPrompt);

        //create new output context
        GoogleCloudDialogflowV2Context loopFinalContext = new GoogleCloudDialogflowV2Context();
        if(itemList.size() > 0)
        {
            loopFinalContext.setName(request.getSession() + loop);
        }
        else
        {
            loopFinalContext.setName(request.getSession() + finalLoop);
        }

        loopFinalContext.setLifespanCount(1);

        //add created context to existing
        List<GoogleCloudDialogflowV2Context> contextList = request.getQueryResult().getOutputContexts();
        contextList.add(loopFinalContext);
        response.setOutputContexts(contextList);
        response.setFulfillmentText("Are you knowledgeable about " + lastElementPrompt + "?");

    }

    private void getItemInfo(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type){
        String parameterName = "";
        List<String> currentItem;

	    if(type.equals("Skill")){
            parameterName = "current_skill";
            currentItem = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
            Skills skillObject = skills.findByName(currentItem.get(0));
            response.setFulfillmentText(skillObject.getDescription() + " So are you familiar with it?");
        }
        else if(type.equals("Tool")){
            parameterName = "current_tool";
            currentItem = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
            Tools toolObject = tools.findByName(currentItem.get(0)).get(0);
            response.setFulfillmentText(toolObject.getDescription() + " So are you familiar with it?");
        }

    }

    private void goToTools(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type){
        String toolContext = "/contexts/awaiting_goal_tool_subdomain";

	    //create new output context
        GoogleCloudDialogflowV2Context loopFinalContext = new GoogleCloudDialogflowV2Context();
        loopFinalContext.setName(request.getSession() + toolContext);
        loopFinalContext.setLifespanCount(1);

        //add created context to existing
        List<GoogleCloudDialogflowV2Context> contextList = request.getQueryResult().getOutputContexts();
        contextList.add(loopFinalContext);
        response.setOutputContexts(contextList);

        returnSubdomains(request, response, type);
    }

    private void loopAllSubdomains(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type){
	    List<String> uniqueSubdomains = extractParameter(request, request.getSession() + "/contexts/session_variable", "uniqueSubdomainList");
        String parameterName ="";

	    if(type.equals("Skill")) {
            parameterName = "skill_subdomain_type";
        }
        else{
            parameterName = "tool_subdomain_type";
        }
        addParameter(request,request.getSession() + "/contexts/session_variable", parameterName, uniqueSubdomains );

        returnListOfItems(request, response, type);
    }

    private void finalItemList(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response, String type, String userResponse){
        String parameterName = "";
        //List<String> itemList = extractParameter(request, request.getSession() + "/contexts/session_variable", "item_list_loop");

        if(type.equals("Skill")){
            parameterName = "current_skill";
            if(userResponse.equals("Yes"))
            {
                List<String> currentSkill = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
                Skills skillFound = skills.findByName(currentSkill.get(0));
                employeeService.addSkillToEmployee(skillFound, demoEmployee);
            }

            returnSubdomains(request, response, "Tool");
        }
        else if(type.equals("Tool")){
            parameterName = "current_tool";

            if(userResponse.equals("Yes"))
            {
                List<String> currentTool = extractParameter(request, request.getSession() + "/contexts/session_variable", parameterName);
                employeeService.addToolToEmployee(tools.findByName(currentTool.get(0)).get(0), demoEmployee);
            }

            List<String> extractedTool = extractParameter(request, request.getSession() + "/contexts/session_variable", "current_tool");
            StringBuilder responseString = new StringBuilder("");
            //if no tools were needed
            if(extractedTool.get(0).equals("-1"))
            {
                responseString.append("Congratulations, it appears you are already familiar with all the tools necessary for the selected subdomains.<br>");
            }

            //TODO handle final item in tool, PDF and such
            //sendEmail();
            sendPdf(request);
            response.setFulfillmentText(responseString.toString() + "Thank you for the chat! I'll send you an email for a recommendation plan on the courses that will best help you advance in your career path.<br>");
        }
    }


    private void sendPdf(GoogleCloudDialogflowV2WebhookRequest request){
  	    List<String> jobName = extractParameter(request, request.getSession() + "/contexts/session_variable", "job_type");
  	    List<Jobs> job = jobs.findAllByNameContains(jobName.get(0));
  	    List<Skills> fullSkillList = skills.findByJobs(job.get(0));
  	    List<Tools> fullToolList = tools.findByJobs(job.get(0));

  	    List<Employees> emp = employees.findAllByEmailAddressContaining(demoEmployee.getEmailAddress());
  	    List<Skills> fullEmployeeSkill = skills.findByEmployees(emp.get(0));
        List<Tools> fullEmployeeTool = tools.findByEmployees(emp.get(0));

        //removing known skills
        for (Skills skill: fullEmployeeSkill) {
            fullSkillList.remove(skill);
        }


        //removing known tools
        for (Tools tool: fullEmployeeTool) {
            fullToolList.remove(tool);
        }

		int timeToComplete = 0;

		List<Tools> beginnerTools = new ArrayList<>();
		List<Courses> beginnerToolCourses = new ArrayList<>();
		List<Tools> advancedTools = new ArrayList<>();
		List<Courses> advancedToolCourses = new ArrayList<>();
		List<Skills> beginnerSkills = new ArrayList<>();
		List<Courses> beginnerSkillCourses = new ArrayList<>();
		List<Skills> advancedSkills = new ArrayList<>();
		List<Courses> advancedSkillCourses = new ArrayList<>();

        for (Tools tool: fullToolList) {
            Courses course = courses.findByCourseId(tool.getCourseID());
			timeToComplete += Integer.parseInt(course.getDuration());
			if (course.getLevel().equals("Beginner")){
				beginnerTools.add(tool);
				beginnerToolCourses.add(course);
			}
			else{
				advancedTools.add(tool);
				advancedToolCourses.add(course);
			}
        }

        for (Skills skill: fullSkillList) {
            Courses course = courses.findByCourseId(skill.getCourseID());
			timeToComplete += Integer.parseInt(course.getDuration());
			if (course.getLevel().equals("Beginner")){
				beginnerSkills.add(skill);
				beginnerSkillCourses.add(course);
			}
			else{
				advancedSkills.add(skill);
				advancedSkillCourses.add(course);
			}
        }

		StringBuilder pdfData = new StringBuilder();

		pdfData.append("Estimated Time to Completion: " + timeToComplete + " weeks.\n\n\n");
        pdfData.append("***Skills necessary to Learn: " + fullSkillList.size() +  "***\n\n");
		pdfData.append("***Beginner Skills***\n\n");
        for (int i =0; i< beginnerSkills.size(); i++) {
            pdfData.append("	 " + beginnerSkills.get(i).getName() + "\n");
            pdfData.append("     " + beginnerSkillCourses.get(i).getUrl() + "\n\n");
        }
        
		pdfData.append("***Advanced Skills***\n\n");
        for (int i =0; i< advancedSkills.size(); i++) {
            pdfData.append("	 " + advancedSkills.get(i).getName() + "\n");
            pdfData.append("     " + advancedSkillCourses.get(i).getUrl() + "\n\n");
        }

        pdfData.append("\n\n***Tools necessary to Learn: " + fullToolList.size() +  "***\n");
		pdfData.append("***Beginner Tools***\n\n");
        for (int i =0; i< beginnerTools.size(); i++) {
            pdfData.append("	 " + beginnerTools.get(i).getName() + "\n");
            pdfData.append("     " + beginnerToolCourses.get(i).getUrl() + "\n\n");
        }

        pdfData.append("***Advanced Tools***\n\n");
        for (int i =0; i< advancedTools.size(); i++) {
            pdfData.append("	 " + advancedTools.get(i).getName() + "\n");
            pdfData.append("     " + advancedToolCourses.get(i).getUrl() + "\n\n");
        }

        sendEmail(pdfData);
    }

    private void sendEmail(StringBuilder pdfData)
    {
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", host);
        properties.put("mail.smtp.user", USER_NAME);
        properties.put("mail.smtp.password", PASSWORD);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties);

	    //To email is useremail
	    String content = "Thanks for using the ATOS development chatbot, based on our conversation these are the skills that I believe you should learn. If you have any questions please feel free to contact support.";
	    String subject = "TrainingBot Report";

	    ByteArrayOutputStream outputStream = null;
	    try
	    {
            //creating mime Message
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(USER_NAME));
            InternetAddress toAddress = new InternetAddress();
            toAddress = new InternetAddress(RECIPIENT);
            mimeMessage.addRecipient(Message.RecipientType.TO, toAddress);

			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setText(content);

			//writing pdf content to outputStream
		    outputStream = new ByteArrayOutputStream();
		    writePdf(outputStream, pdfData);
		    byte[] bytes = outputStream.toByteArray();

		    //creating pdf body content
		    DataSource dataSource = new ByteArrayDataSource(bytes,"application/pdf");
		    MimeBodyPart pdfBodyPart = new MimeBodyPart();
		    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
		    pdfBodyPart.setFileName("TrainingBotRecommendations.pdf");

		    //constructive mime multi part
		    MimeMultipart mimeMultipart = new MimeMultipart();
		    mimeMultipart.addBodyPart(textBodyPart);
		    mimeMultipart.addBodyPart(pdfBodyPart);

		    //sender and recipient address
		    InternetAddress iaSender = new InternetAddress(USER_NAME);
		    InternetAddress iaRecipient = new InternetAddress(RECIPIENT);


		    mimeMessage.setSubject(subject);
		    mimeMessage.setContent(mimeMultipart);

		    //sending email
            Transport transport = session.getTransport("smtp");
            transport.connect(host, USER_NAME, PASSWORD);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
	    }
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	    }
	    finally
	    {
		    //cleaning stream
		    if(outputStream != null)
		    {
		    	try
			    {
			    	outputStream.close();
			    	outputStream = null;
			    }
			    catch(Exception ex)
			    {
			    }
		    }
	    }
    }

	/**
	 * Writes the content of a PDF file (using iText API) to the outputStream
	 * @param outputStream {@link OutputStream}.
	 * @throws Exception
	 */


	private void writePdf(OutputStream outputStream, StringBuilder pdfData) throws Exception
	{
		Document document = new Document();
		PdfWriter.getInstance(document,outputStream);
		document.open();

		//writing to the document pdf
		document.addTitle("Recommendation from ATOS Chatbot");
		document.addSubject("Course Recommendation");
		document.addCreator("ATOS Chatbot");
		document.addCreationDate();

		Paragraph paragraph = new Paragraph();
		//add image here 

		/*
		String imageURL = "https://i.dlpng.com/static/png/349664_thumb.png"; //could also place image in this directory? #groupAppEngine/src/main/resources/sampleletterhead.png
		Image image = Image.getInstance(new URL(imageURL));
		document.add(image);
		*/
		paragraph.add(new Chunk(pdfData.toString())); // TODO DATA FROM COURSE REC GOES HERE
		document.add(paragraph);

		document.close();
	}

	//TODO function to set the information of the employee if the ID is correct.
	private void checkID(GoogleCloudDialogflowV2WebhookRequest request, GoogleCloudDialogflowV2WebhookResponse response){
		List<String> gotID = extractParameter(request, request.getSession() + "/contexts/session_variable", "employee_id");
		//GoogleCloudDialogflowV2Context foundContext = findContext(request, "/contexts/session_variable");
		//String parameterList = foundContext.getParameters().get("employee_id").toString();
		String testID = gotID.get(0);
		double numericalID = Double.parseDouble(testID);
		long employeeID = (long)numericalID;
		demoEmployee = employees.findByEmployeeId(employeeID);
		if(demoEmployee != null){
			RECIPIENT = demoEmployee.getEmailAddress();
			employeeService.saveEmployee(demoEmployee);
			//now just add the stuff for the dialogflow response
			response.setFulfillmentText("Hello " + demoEmployee.getFirstName() + " " + demoEmployee.getLastName() + "! How may I help you today?");
		}
		else {
			//now just add the stuff for the dialogflow response
			response.setFulfillmentText("I'm sorry. I didn't find that ID, please try again.");
		}
	}

}
