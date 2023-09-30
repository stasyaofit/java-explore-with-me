# java-explore-with-me
Template repository for ExploreWithMe project.
Link PR: [https://github.com/stasyaofit/java-explore-with-me/pull/4]
# ExploreWithMe - приложение, позволяющее пользователям делиться информацией об интересных событиях и находить попутчиков для участия в них.
# Modules
## service: The main application module.
## stats: The statistics parent module.
## stats-server: Statistics collection server.
## stats-dto: DTO library for the statistics server.
## stats-client: Client for interacting with the statistics server.
# API stats-server
https://github.com/stasyaofit/java-explore-with-me/blob/main/ewm-stats-service-spec.jsonservice

# API main-service
https://github.com/stasyaofit/java-explore-with-me/blob/main/ewm-main-service-spec.json

# Getting Started
## To get started with ExploreWithMe, follow these steps:

### Clone the repository: git clone https://github.com/stasyaofit/java-explore-with-me.git
### Navigate to the project directory: cd java-explore-with-me
### Set up and configure each module as needed. 
### Build and run the application.
# Additional feature
## Additional application functionality - comments:

### Registered users can leave comments on events.
### Viewing comments on an event is available to all users.
### Comment authors and administrators can edit or delete comments .
# Postman test collections
### stats-server detached
https://github.com/stasyaofit/java-explore-with-me/blob/feature_comments/postman/Tests%20for%20detatched%20stats%20service.json
### main service + stats service
https://github.com/stasyaofit/java-explore-with-me/blob/feature_comments/postman/Test%20Explore%20With%20Me%20-%20Main%20service.json
### main service - comments
https://github.com/stasyaofit/java-explore-with-me/blob/feature_comments/postman/feature.json