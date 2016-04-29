# CodingChallenge

The CodingChallenge Android Application fetches the data from the rails/rails WebAPI. 
And displays it on the UI using ListView. The JSON data received from the http GET is
parsed and stored in an Arraylist of Hashmaps<String,String>. In order to display this 
data we use SimpleAdapter to bind the ArrayList object to the ListView. 

On clicking a ListView entry onClickListener invokes a dialog to display the comments for
associated Issue. 

AsyncTasks are used for network communication to avoid the UI thread from hanging during 
errors or slow networks.

CCActivity is the main Activity which has nested classes GetComments and GetIssues.
GithubIssuesHelper is a class which downloads the json data.

