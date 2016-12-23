(ns pages.home)

(defn component []
  [:div
    [:div {:class "jumbotron"}
      [:div {:class "container"}
        [:h1 "Hello, world!"]
        [:p "This project aim to let user create easily their own serverless
          applications. This is a huge help for start-ups to build
          low cost solutions with high scalability. This project let your
          newt innovation decreasing his time to market and make it more
          environment friendly by only running what you need when requested."]
        [:p
          [:a {:class "btn btn-primary btn-lg" :href "#" :role "button"}
            "Get started »"]]]]
    [:div {:class "container"}
      [:div {:class "row"}
        [:div {:class "col-md-4"}
          [:h2 "Develop faster"]
          [:p "Because we think User experience is really important for your
          product and user don't care about infastructures, we allow teams to
          focus on resources that keep users happy. Serverless keep teams more
          agile and faster to test functionalities."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]
        [:div {:class "col-md-4"}
          [:h2 "Auto-scaling"]
          [:p "You probably have the next Facebook in mind, and you care about
            keeping a system up 24/7. Don't be unprepared when success strikes.
            Serverless architecture means you don’t need to think about it."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]
        [:div {:class "col-md-4"}
          [:h2 "Data as a Service"]
          [:p "Donec id elit non mi porta gravida at eget metus.
            Fusce dapibus, tellus ac cursus commodo, tortor mauris
            condimentum nibh, ut fermentum massa justo sit amet
            risus. Etiam porta sem malesuada magna mollis euismod.
            Donec sed odio dui."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]]]])
