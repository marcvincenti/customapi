(ns aws.regions)

(defn list-regions
  "Return the list of available regions in aws"
  []
  (list {:name "US East (N. Virginia)" :value "us-east-1"}
   {:name "US East (Ohio)" :value	"us-east-2"}
   {:name "US West (N. California)" :value "us-west-1"}
   {:name "US West (Oregon)" :value	"us-west-2"}
   {:name "Asia Pacific (Mumbai)" :value "ap-south-1"}
   {:name "Asia Pacific (Seoul)" :value	"ap-northeast-2"}
   {:name "Asia Pacific (Singapore)" :value	"ap-southeast-1"}
   {:name "Asia Pacific (Sydney)" :value "ap-southeast-2"}
   {:name "Asia Pacific (Tokyo)" :value	"ap-northeast-1"}
   {:name "EU (Frankfurt)" :value	"eu-central-1"}
   {:name "EU (Ireland)" :value	"eu-west-1"}
   {:name "South America (São Paulo)" :value "sa-east-1"}))
