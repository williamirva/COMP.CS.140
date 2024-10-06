const express = require('express');
const os = require('os');
const { exec } = require('child_process');

const app = express();
const port = 8080;

const executeCommand = (command) => {
    return new Promise((resolve, reject) => {
        exec(command, (error, stdout, stderr) => {
            if (error) {
                reject(error);
            } else if (stderr) {
                reject(stderr);
            } else {
                resolve(stdout.trim());
            }
        });
    });
};

app.get('/system-info', async (req, res) => {
    try {
        const ipAddress = os.networkInterfaces();
        const ip = Object.values(ipAddress).flat().find(i => i.family === 'IPv4' && !i.internal).address;
        const processes = await executeCommand('ps -aux');
        const diskSpace = await executeCommand('df -h');
        const uptime = await executeCommand('uptime -p');

        res.json({
            ipAddress: ip,
            processes: processes,
            diskSpace: diskSpace,
            uptime: uptime
        });
    } catch (error) {
        res.status(500).json({ error: 'An error occurred while gathering system information', details: error.message });
    }
});

app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
