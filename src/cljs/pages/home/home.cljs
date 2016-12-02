(ns pages.home)

(defn component []
  [:div
    [:div {:class "jumbotron"}
      [:div {:class "container"}
        [:h1 "Hello, world!"]
        [:p "This is a template for a simple marketing or informational
          website. It includes a large callout called a jumbotron and three
          supporting pieces of content. Use it as a starting point to create
           something more unique."]
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
          [:h2 "Pay-per-execution"]
          [:p "Donec id elit non mi porta gravida at eget metus.
            Fusce dapibus, tellus ac cursus commodo, tortor mauris
            condimentum nibh, ut fermentum massa justo sit amet
            risus. Etiam porta sem malesuada magna mollis euismod.
            Donec sed odio dui."]
          [:p [:a {:class "btn btn-default" :href "#" :role "button"}
          "View details »"]]]]]])
