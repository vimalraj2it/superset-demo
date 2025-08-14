# Brand Dashboard + Redash Reports

This project is a full-stack web application that provides a brand-specific dashboard populated with BI reports. It uses Next.js for the frontend, Spring Boot for the backend, MongoDB as the primary data store, and Trino and Redash for BI and report embedding. The entire stack is orchestrated with Docker Compose.

This project is a migration from the original Superset-based implementation to use Redash for embedded analytics.

## Architecture

The application consists of the following services:

-   **Frontend:** A Next.js (React) application that provides the user interface.
-   **Backend:** A Spring Boot (Java) application that handles authentication, data access, and generates signed URLs for embedding Redash reports.
-   **Database:** A MongoDB instance for storing user and brand data.
-   **Query Engine:** A Trino instance that connects to MongoDB, allowing for federated queries.
-   **BI Tool:** A Redash instance that connects to Trino to build and visualize dashboards.

All services are containerized and managed by `docker-compose.yml`.

## Features

-   JWT-based authentication for the web application.
-   Secure, signed-URL embedding of Redash dashboards.
-   Users can only view dashboards for brands they are authorized to see.
-   Dashboard-level parameterization is used to filter data for the correct brand.
-   A sample data loader to get started quickly.

## Prerequisites

-   [Docker](https://docs.docker.com/get-docker/)
-   [Docker Compose](https://docs.docker.com/compose/install/) (v2.x, i.e., the `docker compose` command, not `docker-compose`)

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd <repository-directory>
    ```

2.  **Environment Configuration:**
    Review and change the default secrets in the following files:
    -   **Backend JWT Secret:** `backend/src/main/resources/application.properties` -> `jwt.secret`
    -   **Redash Secrets:** `redash.env` -> `REDASH_COOKIE_SECRET` and `REDASH_SECRET_KEY`.
    -   **Redash API Key:** `backend/src/main/resources/application.properties` -> `redash.api_key`. You will get this from the Redash UI in a later step.

3.  **Initialize Redash Database:**
    Before the first run, you need to initialize the Redash database schema.
    ```bash
    sudo docker compose run --rm redash-server create_db
    ```

4.  **Build and run the application:**
    From the root of the project, run:
    ```bash
    sudo docker compose up --build -d
    ```
    This will build the images for the frontend and backend services and start all containers. It may take a few minutes on the first run.

## Accessing the Services

-   **Frontend Application:** [http://localhost:3000](http://localhost:3000)
-   **Backend API:** [http://localhost:8080](http://localhost:8080)
-   **Redash:** [http://localhost:5001](http://localhost:5001)
-   **Trino UI:** [http://localhost:8090](http://localhost:8090)

## Credentials

-   **Web Application:**
    -   **Username:** `om-stage@ausmit.in`
    -   **Password:** `password`
-   **Redash:** You will create your own admin account on the first visit to [http://localhost:5001](http://localhost:5001).

## Redash Configuration (Manual Steps)

After starting the application, you need to perform a one-time setup in Redash to create and embed a dashboard.

### Step 1: Connect Trino as a Data Source

1.  Log in to Redash at [http://localhost:5001](http://localhost:5001).
2.  Navigate to **Settings** > **Data Sources**.
3.  Click the **+ New Data Source** button and select **Trino**.
4.  Configure the connection:
    -   **Name:** `trino`
    -   **Host:** `trino` (the service name from `docker-compose.yml`)
    -   **Port:** `8080`
    -   **Username:** `redash` (or any other username)
    -   **Catalog:** `mongodb`
    -   **Schema:** `brand-dashboard`
5.  Click **Create**, then **Test Connection** to verify.

### Step 2: Create a Parameterized Query

1.  Click **Create** > **New Query**.
2.  Select the `trino` data source.
3.  Write a SQL query that uses a parameter to filter by brand ID. Redash uses `{{ parameter_name }}` syntax for parameters.
    ```sql
    SELECT * FROM "mongodb"."brand-dashboard".brands
    WHERE brand_id = '{{ brand_id }}'
    ```
4.  In the parameter settings below the query editor, give the `brand_id` parameter a default value (e.g., `1`) to test the query.
5.  Save the query (e.g., "Brand Data by ID").

### Step 3: Create a Dashboard and Get its ID

1.  Click **Create** > **New Dashboard**.
2.  Give the dashboard a name (e.g., "Brand Details").
3.  Add the query widget you created in the previous step to the dashboard.
4.  When adding the widget, map the dashboard's `brand_id` parameter to the query's `brand_id` parameter.
5.  Publish the dashboard.
6.  View your dashboard. The URL will look like this: `http://localhost:5001/dashboards/1-brand-details`. The `1` is the ID of the dashboard.

### Step 4: Get Your User API Key

1.  In Redash, click your user profile icon in the top right and select your profile.
2.  On the left, you will see your **API Key**. Copy this key.

### Step 5: Update Backend Configuration

The backend needs the dashboard ID and your Redash API key to generate the correct signed URL.

1.  Open `backend/src/main/resources/application.properties`.
2.  Set the value for `redash.dashboard.id` to the ID you found in Step 3.
3.  Set the value for `redash.api_key` to the key you copied in Step 4.

### Step 6: Restart and Verify

1.  Restart the backend service to apply the new configuration:
    ```bash
    sudo docker compose restart backend
    ```
2.  Log into the main application at [http://localhost:3000](http://localhost:3000) and navigate to a brand's detail page. The Redash dashboard should now be embedded and filtered correctly for that brand.
