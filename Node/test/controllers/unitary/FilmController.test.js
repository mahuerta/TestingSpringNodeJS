const app = require('../../../src/app')
const supertest = require('supertest')
const request = supertest(app);

const AWS = require('aws-sdk');
jest.mock('aws-sdk');

test('Get films', async () => {
  let data = {
    "Items": [
      {
        "id": 1,
        "titulo": "TITULO"
      },
      {
        "id": 2,
        "titulo": "TITULO 2"
      }
    ]
  }

  AWS.DynamoDB.DocumentClient.prototype.scan.mockImplementation((_, cb) => {
    cb(null, data);
  });

  const response = await request.get('/api/films/')
  .expect('Content-type', /json/)
  .expect(200)

  expect(response.body).toEqual(expect.arrayContaining(data.Items));

})

test('Get no films', async () => {
  let data = {
    "Items": []
  }

  AWS.DynamoDB.DocumentClient.prototype.scan.mockImplementation((_, cb) => {
    cb(null, data);
  });

  const response = await request.get('/api/films/')
  .expect('Content-type', /json/)
  .expect(200)

  expect(response.body).toEqual(expect.arrayContaining(data.Items));

})

// TODO: Posiblemente sea necesario meter más validaciones de la película creada
test('Create film', async () => {
  let film = {
    "titulo": "TITULO"
  };

  AWS.DynamoDB.DocumentClient.prototype.put.mockImplementation((_, cb) => {
    cb(null, film);
  });

  const response = await request.post('/api/films/')
  .expect('Content-type', /json/)
  .send(film)
  .expect(201)

  expect(response.body.id).toEqual((expect.any(Number)));

})