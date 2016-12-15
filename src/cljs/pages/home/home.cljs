(ns pages.home)

(defn component []
  [:div
    [:div {:class "jumbotron"}
      [:div {:class "container"}
        [:h1 "Hello, world!"]
        [:p "This project aim to create back-end services easily in the cloud,
          and quickly add a ton of availables microservices.
          You will be able to deploy your applications as independent functions,
          that respond to events and scale automatically."]
        [:p
          [:a {:class "btn btn-primary btn-lg" :href "#" :role "button"}
            "Get started »"]]]]
    [:div {:class "container"}
      [:div {:class "row"}
        [:div {:class "col-md-4"}
          [:h2 "Serverless"]
          [:p "Donec id elit non mi porta gravida at eget metus.
            Fusce dapibus, tellus ac cursus commodo, tortor mauris
            condimentum nibh, ut fermentum massa justo sit amet
            risus. Etiam porta sem malesuada magna mollis euismod.
            Donec sed odio dui."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]
        [:div {:class "col-md-4"}
          [:h2 "Auto-scaling"]
          [:p "Donec id elit non mi porta gravida at eget metus.
            Fusce dapibus, tellus ac cursus commodo, tortor mauris
            condimentum nibh, ut fermentum massa justo sit amet
            risus. Etiam porta sem malesuada magna mollis euismod.
            Donec sed odio dui."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]
        [:div {:class "col-md-4"}
          [:h2 "Microservices market-place"]
          [:p "Donec id elit non mi porta gravida at eget metus.
            Fusce dapibus, tellus ac cursus commodo, tortor mauris
            condimentum nibh, ut fermentum massa justo sit amet
            risus. Etiam porta sem malesuada magna mollis euismod.
            Donec sed odio dui."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]]]])
