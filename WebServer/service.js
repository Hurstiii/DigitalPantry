require("sqlite-async");
let { Log } = require("./debug_logger");
Log.verbose = false; // show verbose debugging

async function getProducts(db, onComplete) {
  let log_base = `/products : GET : getProducts =>`;
  let data = [];
  let count = 0;
  let query = "SELECT * FROM products ORDER BY name ASC";
  await db
    .each(query, (err, row) => {
      if (err) {
        console.log(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, row);
      }
    })
    .catch((err) => {
      return onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      return onComplete(undefined, count, data);
    });
}

async function getProductsWithTags(db, onComplete) {
  let log_base = `/products : GET : getProductsWithTags =>`;
  return getProducts(db, async (err, count, data) => {
    await Promise.allSettled(
      data.map(async (item) => {
        return getTagsForProduct(
          db,
          item.barcode,
          (tag_err, tag_count, tags) => {
            if (tag_err) {
              console.error(tag_err);
            }
            item.tags = tags;
            Log.ver(
              `${log_base} added following tags to item ${item.barcode}`,
              tags
            );
          }
        );
      })
    )
      .catch((err) => {
        console.error(err.message);
        return onComplete(err, count, data);
      })
      .then(() => {
        Log.ver(`${log_base} data = `, data);
        return onComplete(undefined, count, data);
      });
  });
}

async function getTagsForProduct(db, barcode, onComplete) {
  let log_base = `getTagsForProducts => `;
  let tags = [];
  let count = 0;
  let tags_query =
    "SELECT tags.colour, tags.tag " +
    "FROM products " +
    "INNER JOIN (" +
    "SELECT * FROM prod_tags INNER JOIN tags ON prod_tags.tag_id = tags.id" +
    ") as tags ON products.barcode = tags.barcode " +
    "AND tags.barcode = " +
    `"${barcode}"` +
    " " +
    "ORDER BY tags.tag ASC";

  await db.each(tags_query, function (err, tag_row) {
    if (err) {
      console.error(`${log_base} ${err.errno} : ${err.message}`);
    } else {
      tags.push(tag_row);
      count++;
    }
  });
  Log.ver(`${log_base} ${count} tags found for product ${barcode}`);
  return onComplete(undefined, count, tags);
}

async function getProductsItem(db, barcode, onComplete) {
  let log_base = `/products/${barcode} : GET : getProductsItem =>`;
  let data = [];
  let count = 0;
  let query = `SELECT * FROM products WHERE barcode="${barcode}" ORDER BY name ASC`;
  await db
    .each(query, (err, row) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, data);
      }
    })
    .catch((err) => {
      return onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      return onComplete(undefined, count, data);
    });
}

async function getProductsItemWithTags(db, barcode, onComplete) {
  let log_base = `/products/${barcode} : GET : getProductsItemWithTags =>`;
  return getProductsItem(db, barcode, async (err, count, data) => {
    await Promise.allSettled(
      data.map(async (item) => {
        return getTagsForProduct(
          db,
          item.barcode,
          (tag_err, tag_count, tags) => {
            if (tag_err) {
              console.error(tag_err);
            }
            item.tags = tags;
            Log.ver(
              `${log_base} added following tags to item ${item.barcode}`,
              tags
            );
          }
        );
      })
    )
      .catch((err) => {
        console.error(err.message);
        return onComplete(err, count, data);
      })
      .then(() => {
        Log.ver(`${log_base} data = `, data);
        return onComplete(undefined, count, data);
      });
  });
}

async function getPantry(db, onComplete) {
  let log_base = `/pantry : GET : getPantry =>`;
  let data = [];
  let count = 0;
  let query =
    "SELECT * FROM pantry INNER JOIN products ON pantry.barcode = products.barcode ORDER BY products.name ASC";
  await db
    .each(query, function (err, row) {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, row);
      }
    })
    .catch((err) => {
      return onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      return onComplete(undefined, count, data);
    });
}

async function getPantryWithTags(db, onComplete) {
  let log_base = `/pantry : GET : getPantry =>`;
  return getPantry(db, async (err, count, data) => {
    await Promise.allSettled(
      data.map(async (item) => {
        return getTagsForProduct(
          db,
          item.barcode,
          (tag_err, tag_count, tags) => {
            if (tag_err) {
              console.error(tag_err);
            }
            item.tags = tags;
            Log.ver(
              `${log_base} added following tags to item ${item.barcode}`,
              tags
            );
          }
        );
      })
    )
      .catch((err) => {
        console.error(err.message);
        return onComplete(err, count, data);
      })
      .then(() => {
        Log.ver(`${log_base} data = `, data);
        return onComplete(undefined, count, data);
      });
  });
}

async function getPantryItem(db, barcode, onComplete) {
  let log_base = `/pantry/${barcode} : GET : getPantryItem =>`;
  let data = [];
  let count = 0;
  let query = `SELECT * FROM pantry INNER JOIN products ON pantry.barcode = products.barcode WHERE pantry.barcode="${barcode}" ORDER BY name ASC`;

  await db
    .each(query, (err, row) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, row);
      }
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd out`, data);
      return onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      return onComplete(undefined, count, data);
    });
}

async function getPantryItemWithTags(db, barcode, onComplete) {
  let log_base = `/pantry/${barcode} : GET : getPantryItemWithTags =>`;
  return getPantryItem(db, barcode, async (err, count, data) => {
    await Promise.allSettled(
      data.map(async (item) => {
        return getTagsForProduct(
          db,
          item.barcode,
          (tag_err, tag_count, tags) => {
            if (tag_err) {
              console.error(tag_err);
            }
            item.tags = tags;
            Log.ver(
              `${log_base} added following tags to item ${item.barcode}`,
              tags
            );
          }
        );
      })
    )
      .catch((err) => {
        console.error(err.message);
        return onComplete(err, count, data);
      })
      .then(() => {
        Log.ver(`${log_base} data = `, data);
        return onComplete(undefined, count, data);
      });
  });
}

async function getTags(db, onComplete) {
  let log_base = `/tags : GET : getTags =>`;
  let data = [];
  let count = 0;
  let query = "SELECT * FROM tags ORDER BY products.name ASC";
  await db
    .each(query, function (err, row) {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, row);
      }
    })
    .catch((err) => {
      return onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      return onComplete(undefined, count, data);
    });
}

async function getTagsItem(db, id, onComplete) {
  let log_base = `/tags/${id} : GET : getTagItem =>`;
  let data = [];
  let count = 0;
  let query = `SELECT * FROM tags WHERE id="${id}" ORDER BY name ASC`;

  await db
    .each(query, (err, row) => {
      if (err) {
        console.error(`${log_base} ${err.errno} : ${err.message}`);
      } else {
        data.push(row);
        count++;
        Log.ver(`${log_base} on row ${count}`, row);
      }
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd out`, data);
      onComplete(err, count, data);
    })
    .then(() => {
      Log.ver(`${log_base} completed`, data);
      onComplete(undefined, count, data);
    });
}

async function insertProducts(db, data, onComplete) {
  let log_base = `/product : POST : insertProduct =>`;
  Log.ver(`${log_base} data passed in was`, data);
  if (!(data instanceof Array)) {
    let err = new Error(`${log_base} data is not an array or object`);
    onComplete(err, 0);
    return;
  }

  values = data
    .map(
      (item) =>
        `("${item.barcode}", "${item.format}", "${item.name}", "${item.unit}", "${item.initial_amount}")`
    )
    .join(",");
  let query = `INSERT INTO products VALUES ${values}`;
  await db
    .run(query)
    .catch((err) => {
      Log.ver(`${log_base} error'd out`);
      return onComplete(err, 0);
    })
    .then((result) => {
      Log.ver(
        `${log_base} completed with ${result.changes} ${
          result.changes === 1 ? "change" : "changes"
        }`
      );
      return onComplete(undefined, result.changes);
    });
}

async function insertPantry(db, data, onComplete) {
  let log_base = `/product : POST : insertPantry =>`;
  let inserts = 0;
  await Promise.all(
    data.map(async (item) => {
      if (item.quantity <= 0) {
        Log.log(
          `${log_base} item ${item.barcode} had quantity less than 1 : therefore insertion skipped`
        );
        return Promise.reject(new Error("No such product"));
      }

      Log.ver(`${log_base} item to insert`, item);
      let value = `("${item.barcode}", "${item.quantity}")`;
      let query = `INSERT INTO pantry VALUES ${value}`;

      /**
       * Check that there is an entry for it in the product table
       */
      return getProductsItem(db, item.barcode, (g_err, count, data) => {
        if (g_err) {
          Log.log(
            `${log_base} Error retrieving product entry for item ${item.barcode}`
          );
          return Promise.resolve(Promise.reject(new Error("No such product")));
        } else if (count !== 1) {
          Log.log(
            `${log_base} Product does not exist in database, cannot insert into pantry`
          );
          return Promise.resolve(Promise.reject(new Error("No such product")));
        } else {
          return db
            .run(query)
            .then((result) => {
              inserts++;
              Log.log(`${log_base} Inserted new item ${item.barcode}`);
              Log.ver(`${log_base} total inserts is ${inserts}`);
            })
            .catch((err) => {
              switch (err.errno) {
                case 19: // Unique constraint violation?
                  Log.ver(
                    `${log_base} item already in pantry : updating quantity`
                  );
                  /**
                   * Item already exists so run a UPDATE SQL query to increase the quantity
                   * 1. Need to get the current entry / quantity
                   * 2. Add on the quantity from request
                   * 3. Update entry with new quantity
                   */
                  // 1. get clashed record
                  getPantryItem(db, item.barcode, (g_err, count, data) => {
                    if (g_err)
                      return console.error(
                        `${log_base} Failed to GET record for ${item.barcode}\n ${g_err.errno} : ${g_err.message}`
                      );
                    // 2. adding quantities together
                    let original = data[0].quantity;
                    let newQuantity = original + item.quantity;

                    // 3. updating the record
                    let body = {
                      barcode: item.barcode,
                      quantity: newQuantity,
                    };
                    updatePantryItem(db, item.barcode, body, (u_err) => {
                      if (u_err) {
                        return console.error(
                          `${log_base} Failed to UPDATE record for ${item.barcode} \n${u_err.errno} : ${u_err.message}`
                        );
                      }
                    });
                  });
                  Log.log(`${log_base} Appended quantity for ${item.barcode}`);
                  inserts++;
                  Log.ver(`${log_base} total inserts is ${inserts}`);
                  return;
              }

              // log error on server console
              return console.error(`${log_base} ${err.errno} : ${err.message}`);
            });
        }
      });
    })
  )
    .then((result) => {
      Log.ver(
        `${log_base} completed with ${inserts} ${
          inserts === 1 ? "inserts" : "inserts"
        }`
      );
      return onComplete(undefined, inserts);
    })
    .catch((err) => {
      Log.ver(
        `${log_base} error'd out with ${inserts} ${
          inserts === 1 ? "inserts" : "inserts"
        }`
      );
      return onComplete(err, inserts);
    });
}

async function updateProductsItem(db, barcode, data, onComplete) {
  let log_base = `/products/${barcode} : PUT : updateProductItem =>`;
  let query = `UPDATE products 
  SET format="${data.format}", name="${data.name}", unit="${data.unit}", initial_amount="${data.initial_amount}"
  WHERE barcode="${barcode}"`;
  Log.ver(`${log_base} data to update to`, data);
  await db
    .run(query)
    .then((result) => {
      Log.ver(`${log_base} completed`);
      onComplete(undefined);
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd`);
      onComplete(err);
    });
}

async function updatePantryItem(db, barcode, data, onComplete) {
  let log_base = `/pantry/${barcode} : PUT : updatePantryItem => `;
  let query = `UPDATE pantry 
  SET quantity=${data.quantity}
  WHERE barcode="${barcode}"`;

  if (data.quantity <= 0) {
    Log.ver(`${log_base} quantity will be 0, so removing from pantry instead`);
    deletePantryItem(db, barcode, onComplete);
    return;
  }

  Log.ver(`${log_base} data to update to`, data);
  await db
    .run(query)
    .then((result) => {
      Log.ver(`${log_base} completed`);
      onComplete(undefined);
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd`);
      onComplete(err);
    });
}

async function deleteProductItem(db, barcode, onComplete) {
  let log_base = `/products/${barcode} : DELETE : deleteProductItem => `;
  let query = `DELETE FROM products WHERE barcode="${barcode}"`;

  await db
    .run(query)
    .then((result) => {
      Log.ver(`${log_base} completed`);
      onComplete(undefined);
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd`);
      onComplete(err);
    });
}

async function deletePantryItem(db, barcode, onComplete) {
  let log_base = `/pantry/${barcode} : DELETE : deletePantryItem => `;
  let query = `DELETE FROM pantry WHERE barcode="${barcode}"`;

  await db
    .run(query)
    .then((result) => {
      Log.ver(`${log_base} completed`);
      onComplete(undefined);
    })
    .catch((err) => {
      Log.ver(`${log_base} error'd`);
      onComplete(err);
    });
}

module.exports = {
  getPantry,
  getPantryItem,
  getProducts,
  getProductsWithTags,
  getTagsForProduct,
  getProductsItem,
  getProductsItemWithTags,
  getPantryWithTags,
  getPantryItemWithTags,
  insertPantry,
  insertProducts,
  updatePantryItem,
  updateProductsItem,
  deletePantryItem,
  deleteProductItem,
};
