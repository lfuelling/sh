# SH

Link shorten service.

## Running

1. Change the passwords:
    - `src/main/resources/server.properties`
    - `docker-compose.yml`
2. (Optional) Change the look and feel:
    - `src/main/resources/html/style.css`
3. Build the application: `docker-compose build`
4. Start the application: `docker-compose up`

The application will run on port `8082`, you can change this in the `docker-compose.yml` file.

## Developing

1. Don't change the passwords!
2. Build the project using any (Java) IDE
3. Run the database:
    - `docker run -it -v "$(pwd)/db:/docker-entrypoint-initdb.d" -p 5432:5432 -e POSTGRES_PASSWORD=changemebabychangeme postgres:alpine`
4. Start the application from the IDE

The application will run on port `8080` by default, this can be changed in `src/main/resources/server.properties`. Note that changing this will require you to update the `docker-compose.yml`.

## Third Party

- [Background image](https://unsplash.com/photos/-c017F-rzew) by [unsplash/@kristimartin](https://unsplash.com/@kristimartin)
 