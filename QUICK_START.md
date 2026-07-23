# 🚀 QUICK START GUIDE

## Prerequisites Checklist
- ✅ Java 17+ installed (`java -version`)
- ✅ Maven 3.6+ installed (`mvn -version`)
- ✅ MySQL 8.0+ installed and running
- ✅ Port 8080 available

## 5-Minute Setup

### 1. MySQL Database (2 minutes)
```bash
# Open MySQL Command Line
mysql -u root -p

# Create database
CREATE DATABASE disaster_db;

# Set password for root user
ALTER USER 'root'@'localhost' IDENTIFIED BY 'sreeja@123';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Build Project (2 minutes)
```bash
cd c:\Users\linga\OneDrive\Documents\Desktop\Disaster-Response_System
mvn clean install
```

### 3. Run Application (1 minute)
```bash
mvn spring-boot:run
```

### 4. Access Dashboard
```
http://localhost:8080/
```

## ✅ Verification

Check these endpoints to confirm everything is working:

```bash
# Health check
curl http://localhost:8080/api/disaster/health

# Get metrics
curl http://localhost:8080/api/disaster/metrics

# View history
curl http://localhost:8080/api/disaster/history
```

## 🎯 First Simulation

1. Go to **Simulate** tab
2. Fill in form:
   - Region: Hyderabad
   - Rainfall: 87 mm
   - River Level: 6.5 m
   - Population: 8,500,000
3. Click **Run Simulation**
4. View results in seconds!

## 🆘 Common Issues

| Issue | Solution |
|-------|----------|
| Database connection error | Check MySQL is running, verify password in application.yml |
| Port 8080 in use | Change port in application.yml or kill process: `netstat -ano \| findstr 8080` |
| Build fails | Delete `target/` folder, run `mvn clean install` |
| Page won't load | Wait 30 seconds, browser refresh (Ctrl+F5) |

## 📞 Need Help?

1. Check README.md for detailed documentation
2. Review console output for error messages
3. Verify MySQL connection: `mysql -u root -p -e "SELECT 1"`

---

**You're all set! 🎉 Disaster Response System is running.**
