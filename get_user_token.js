const http = require('http');
const { exec } = require('child_process');
const url = require('url');
const https = require('https');

// Configuration
const clientId = '470833c0fbd64c03b44c23fa7a532ee4';
const clientSecret = '57a9b2d8e6b24540bf36cd8b078b7797';
const redirectUri = 'http://localhost:8888/callback';
const scopes = 'user-read-private user-read-email'; // Add other scopes if needed

// Create Server
const server = http.createServer((req, res) => {
    const reqUrl = url.parse(req.url, true);

    if (reqUrl.pathname === '/callback') {
        const code = reqUrl.query.code;
        if (code) {
            // Exchange code for token
            getToken(code, (error, data) => {
                if (error) {
                    res.end('Error getting token: ' + error);
                } else {
                    res.end('Success! You can close this window. Check your terminal for the token.');
                    console.log('\n--- AUTHorization SUCCESSFUL ---');
                    console.log('Access Token:', data.access_token);
                    console.log('Refresh Token:', data.refresh_token);
                    console.log('Expires In:', data.expires_in);
                    console.log('--------------------------------\n');
                    process.exit(0);
                }
            });
        } else {
            res.end('No code provided.');
        }
    } else {
        res.end('Spotify Auth Server Running');
    }
});

server.listen(8888, () => {
    const authUrl = `https://accounts.spotify.com/authorize?response_type=code&client_id=${clientId}&scope=${encodeURIComponent(scopes)}&redirect_uri=${encodeURIComponent(redirectUri)}`;
    console.log('Please open the following URL to login:');
    console.log(authUrl);

    // Try to open automatically
    exec(`open "${authUrl}"`);
});

function getToken(code, callback) {
    const tokenUrl = 'https://accounts.spotify.com/api/token';
    const auth = Buffer.from(`${clientId}:${clientSecret}`).toString('base64');

    const postData = `grant_type=authorization_code&code=${code}&redirect_uri=${encodeURIComponent(redirectUri)}`;

    const req = https.request(tokenUrl, {
        method: 'POST',
        headers: {
            'Authorization': `Basic ${auth}`,
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': postData.length
        }
    }, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => {
            try {
                callback(null, JSON.parse(data));
            } catch (e) {
                callback(e);
            }
        });
    });

    req.on('error', (e) => callback(e));
    req.write(postData);
    req.end();
}
