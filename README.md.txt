//github: https://github.com/OishikRoyVienna/Media-Ratings-Platform-MRP-.git

Setup

1. Starte PostgreSQL in Docker oder Podman:
   docker run --name mrp-dao -e POSTGRES_DB=mrp -e POSTGRES_USER=mrp -e POSTGRES_PASSWORD=mrp -p 5432:5432 -d postgres

2. test.bat zum Testen des Programms