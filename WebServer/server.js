const request = require("request") // for making GET request to external API
const express = require("express") // for setting up the web server and handling requests
const parser = require("body-parser") // parses json requests
const sql = require("sqlite-async")
const service = require("./service")
const Joi = require("@hapi/joi")
let { Log } = require("./debug_logger")
const { allow } = require("@hapi/joi")

const db_reset = false;

/**
 * Table schemas
 */
const product_item = Joi.object({
  barcode: Joi.string().max(80).required(),
  name: Joi.string().max(100).required(),
  format: Joi.string().required(),
  unit: Joi.string().required(),
  initial_amount: Joi.number().required(),
})
const product_items = Joi.array().items(
  product_item
).single();

const pantry_item = Joi.object({
  barcode: Joi.string().max(80).required(),
  quantity: Joi.number().max(999).required(),
}).unknown(allow)
const pantry_items = Joi.array().items(
  pantry_item
).single();


/**
 * Connect to database
 */
let db

async function instantiate_tables() {
  let create_tags = "CREATE TABLE IF NOT EXISTS tags (" +
  "id INTEGER IDENTITY NOT NULL PRIMARY KEY, " +
  "colour INTEGER, " +
  "tag VARCHAR(20), " +
  "CONSTRAINT UC_tag UNIQUE (colour, tag)" +
  ")"

  let create_products = "CREATE TABLE IF NOT EXISTS products (" +
  "barcode VARCHAR(80) PRIMARY KEY, " +
  "format VARCHAR(20), " +
  "name VARCHAR(100), " +
  "unit VARCHAR(20), " +
  "inital_amount FLOAT" +
  ")"

  let create_prod_tags = "CREATE TABLE IF NOT EXISTS prod_tags (" +
  "barcode VARCHAR(80), " +
  "tag_id INTEGER, " +
  "PRIMARY KEY (barcode, tag_id), " +
  "FOREIGN KEY (barcode) REFERENCES products(barcoe) ON DELETE CASCADE, " +
  "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE" +
  ")"

  let create_pantry = "CREATE TABLE IF NOT EXISTS pantry (" +
  "barcode VARCHAR(80) PRIMARY KEY, " +
  "quantity FLOAT, " +
  "FOREIGN KEY (barcode) REFERENCES products(barcode) ON DELETE CASCADE" +
  ")"

  await Promise.all([
    db.run(create_products),
    db.run(create_tags)
  ])
  .then(() => {
    db.run(create_prod_tags)
    db.run(create_pantry)
  })
  .catch(err => {
    Log.log(`Failed to create all the tables : Encounted an error`)
  })
}

sql.open("./digital_pantry.db")
  .then(async (_db) => {
    db = _db
    Log.log("Openned the SQlite database.");

    // delete the tables so they can be recreated (inorder to reset the database)
    let tables = ["products", "pantry", "tags", "prod_tags"]
    if (db_reset === true) {
      Log.log(`Clearing database : `)
      await Promise.all(tables.map(item => {
        return db.run(`DROP TABLE IF EXISTS ${item}`)
        .then(() => {
          Log.log(`\tSuccess`)
        })
        .catch(() => {
          Log.log(`\tFailed`)
        })
      }))
      .then(() => {
        Log.log(`Creating tables`)
        instantiate_tables();
      })
      .catch(err => {
        return console.error(err.message)
      })
    } else {
      Log.log(`Creating tables`)
      instantiate_tables();
    }
  })
  .catch(err => {
    return console.error(err.message)
  })


/**
 * Create and start web server
 */
var app = express();
app.use(parser.json());


/**
 * "GET"
 * "/products"
 * Returns all columns of all products in the database
 * format: {"data": [...]}
 */
app.get("/products", (req, res, next) => {
  let log_base = `/product : GET =>`
  service.getProducts(db, (err, count, data) => {
    if (err) {
      Log.log(`${log_base} ${err.errno} : ${err.message}`)
      return next(err)
    }
    if (data.length === 0) {
      Log.log(`${log_base} 404 Not Found : Didn't find any items`);
      res.status(404).json({ "data": data });
    } else {
      Log.log(`${log_base} Success : Retrieved ${count === undefined ? 0 : count} items`);
      res.status(200).json({ "data": data });
    }
  });
});


/**
 * "GET"
 * "/products/{barcode}"
 * Returns all columns for the product with the specified barcode if it exists
 * format: {"data": [...]}
 */
app.get("/products/:barcode", (req, res, next) => {
  let barcode = req.params.barcode
  let log_base = `/products/${barcode} : GET =>`
  service.getProductsItem(db, barcode, (err, count, data) => {
    if (err) {
      Log.log(`${log_base} ${err.errno} : ${err.message}`)
      return next(err);
    }
    if (data.length === 0) {
      Log.log(`${log_base} Item not found in the database : searching external API`)
      request.get(
        `https://api.barcodelookup.com/v2/products?barcode=${barcode}&key=jv77gduzju09rcu0e46njt0mb6fify`,
        function (err, extRes, body) {
          if (err) {
            console.error(`${log_base} ${err.errno} : ${err.message}`)
            return next(err)
          }

          /**
           * Handle response from the external server
           */
          if (extRes.statusCode === 404) {
            Log.log(`${log_base} External API returned 404 : Product not in external database`);
            return res.status(404).json({ "data": data }); // response indicating that the user needs to add the barcode as a new product
          } else if (extRes.statusCode == 403) {
            // 403 = Forbidden i.e the key has expired etc...
            Log.log(`${log_base} External API returned 403 : Forbidden; meaning the api key has expired`)
            return res.status(404).json({ "data": data }); // response indicating that the user needs to add the barcode as a new product
          } else {
            Log.log(body);
            // parse response body and collect the pertinent information.
            let jsonBody = JSON.parse(body);
            let item = jsonBody.products[0];
            let name = item.product_name;

            if (name === undefined || name === "") {
              console.error(`${log_base} 404 : Product found but has no accossiated name`);
              return res.status(404).json({ "data": data });
            }

            // add product to the database.
            let query = `INSERT INTO products
                         VALUES ("${req.params.barcode}", "N/A", "${name}")`
            db.run(query, function (err) {
              if (err) {
                console.error(`${log_base} ${err.errno} : ${err.message}`)
                return next(err)
              }

              // populate 'data' with the product info to be returned.
              data.push({ "barcode": barcode, "format": "N/A", "name": name });
              count = 1;

              /**
               * Found the product in external database and added it to local one
               * Return a OK status and the count of items retrieved
               */
              Log.log(`${log_base} Success : Retrieved ${count} items`);
              res.status(200).json({ "data": data });
            });
          }
        }
      );
    } else {
      /**
       * Found a product with specified barcode
       * Return a OK status and the count of items retrieved
       */
      Log.log(`${log_base} Success : Retrieved ${count} items`);
      res.status(200).json({ "data": data });
    }
  });
});


/**
 * "GET"
 * "/"
 * "/pantry"
 * Returns the full pantry list
 * format: {"data": [...]}
 */
app.get("/pantry", (req, res, next) => {
  let log_base = `/pantry : GET =>`
  service.getPantry(db, (err, count, data) => {
    if (err) {
      Log.log(`${log_base} ${err.errno} : ${err.message}`)
      return next(err)
    }
    if (data.length === 0) {
      Log.log(`${log_base} 200 Success : Empty Pantry`);
      res.status(200).json({ "data": data });
    } else {
      Log.log(`${log_base} Success : Retrieved ${count === undefined ? 0 : count} items`);
      res.status(200).json({ "data": data });
    }
  });
});


/**
 * "GET"
 * "/pantry/{barcode}"
 * Returns the details/status of the specified product in the pantry if it exists (e.g. quantity)
 * format: {"data": [...]}
 */
app.get("/pantry/:barcode", (req, res, next) => {
  let barcode = req.params.barcode
  let log_base = `/pantry/${barcode} : GET =>`
  service.getPantryItem(db, barcode, (err, count, data) => {
    if (err) {
      Log.log(`${log_base} ${err.errno} : ${err.message}`)
      return next(err)
    }
    if (count === 0) {
      Log.log(`${log_base} 404 Not Found : Didn't find any items`);
      res.status(404).json({ "data": data });
    } else {
      Log.log(`${log_base} Success : Retrieved ${count} items`);
      res.status(200).json({ "data": data });
    }
  });
});


/**
 * "POST"
 * "/products"
 * Create a new product record and add it to the products table in the database
 * @param barcode : string
 * @param format : string - barcode format of product
 * @param name : string
 */
app.post("/products", async (req, res, next) => {
  let log_base = `/products : POST =>`
  try {
    const value = await product_items.validateAsync(req.body);

    service.insertProducts(db, value, (err, count) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
        return next(err);
      }
      Log.log(`${log_base} Success : Rows inserted ${count}`);
      res.status(200).send(`Rows inserted ${count}/${value.length === undefined ? 1 : value.length}`);
    });
  } catch (err) {
    next(err);
  }
});


/**
 * "POST"
 * "/pantry"
 * Add a new record to the pantry (barcode will link to a product in the product table)
 * @param barcode (FK) : string
 * @param quantity : string - int
 * format: body = [{...}, {...}, ...]
 */
app.post("/pantry", async (req, res, next) => {
  let log_base = `/pantry : POST =>`
  try {
    const value = await pantry_items.validateAsync(req.body);
    service.insertPantry(db, value, (inserts) => {
      Log.log(`${log_base} Success : Rows inserted ${inserts}`);
      res.status(200).send(`Rows inserted ${inserts}/${value.length === undefined ? 0 : value.length}`);
    })
  } catch (err) {
    // not a valid request body
    next(err);
  }
});


/**
 * "PUT"
 * "/products/{barcode}"
 * Edit the record of a specific product
 * @param barcode : string - nullable
 * @param format : string - nullable
 * @param name : string - nullable
 */
app.put("/products/:barcode", async (req, res, next) => {
  let log_base = `/products/${req.params.barcode} : PUT =>`
  try {
    const value = await product_item.validateAsync(req.body);
    service.updateProductsItem(db, req.params.barcode, value, (err) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`)
        return next(err)
      }
      res.status(200).send(`${log_base} Record updated`);
      return Log.log(`${log_base} Record updated`);
    });
  } catch (err) {
    next(err);
  }
});


/**
 * "PUT"
 * "/pantry/{barcode}"
 * Edit a specific pantry entry (change quantity)
 * @param barcode : string - nullable
 * @param quantity : string - nullable
 */
app.put("/pantry/:barcode", async (req, res, next) => {
  let log_base = `/pantry/${req.params.barcode} : PUT =>`
  try {
    const value = await pantry_item.validateAsync(req.body);
    service.updatePantryItem(db, req.params.barcode, req.body, (err) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`)
        return next(err)
      }
      res.status(200).send(`${log_base} Record updated`);
      return Log.log(`${log_base} Record updated`);
    });
  } catch (err) {
    next(err);
  }
});


/**
 * "DELETE"
 * "/products/{barcode}"
 * Deletes a specific product entry
 *  + Should cascade to pantry entry too if the product is in the pantry
 */
app.delete("/products/:barcode", (req, res, next) => {
  let log_base = `/products/${req.params.barcode} : DELETE =>`
  service.deleteProductItem(db, req.params.barcode, (err) => {
    if (err) {
      console.error(`${log_base} ${err.errno} : ${err.message}`)
      return next(err)
    }
    res.status(200).send(`${log_base} Record deleted`);
    return Log.log(`${log_base} Record deleted`);
  });
});


/**
 * "DELETE"
 * "/pantry/{barcode}"
 * Removes an entry from the pantry
 */
app.delete("/pantry/:barcode", (req, res, next) => {
  let log_base = `/pantry/${req.params.barcode} : DELETE =>`
  service.deletePantryItem(db, req.params.barcode, (err) => {
    if (err) {
      console.error(`${log_base} ${err.errno} : ${err.message}`)
      return next(err)
    }
    res.status(200).send(`${log_base} Record deleted`);
    return Log.log(`${log_base} Record deleted`);
  });
});


// Start the web server and start listening
app.listen(7000, () => {
  Log.log("Server is running on port 7000");
});
